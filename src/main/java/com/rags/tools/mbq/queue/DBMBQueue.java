package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.*;
import java.util.List;

public class DBMBQueue extends AbstractMBQueue {

    private static final String GET_BY_QUEUE_AND_ID = "select * form MBQueueMessage where Id=:id";
    private static final String GET_BY_QUEUE_SEQ_AND_STATUS = "select * form MBQueueMessage where QueueName=:queue and Sequence=:seq and Status in (:status)";
    private static final String GET_BY_QUEUE_AND_IDS = "select * form MBQueueMessage where QueueName=:queue Id in (:ids)";

    private static final String INSERT_MBQ_MESSAGE = "insert into MBQueueMessage values (:id,:queue,:seq,:status,:data,createTS,:updatedTS)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DBMBQueue() {
        String url = "jdbc:jtds:sybase://server:5019/database";
        try {
            DriverManager.registerDriver((Driver) Class.forName("net.sourceforge.jtds.jdbc.Driver").getDeclaredConstructor().newInstance());
            Connection conn = DriverManager.getConnection(url, "me", "pass");
            SingleConnectionDataSource ds = new SingleConnectionDataSource(conn, true);
            jdbcTemplate = new NamedParameterJdbcTemplate(ds);
        } catch (Exception e) {
            throw new RuntimeException("Error in connection");
        }
    }

    @Override
    public MBQMessage get(String queueName, String id) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("queue", queueName)
                .addValue("id", id);
        List<MBQMessage> messages = jdbcTemplate.query(GET_BY_QUEUE_AND_ID, param, new MessageRowMapper());
        return messages.isEmpty() ? null : messages.get(0);
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("queue", queueName)
                .addValue("seq", seqKey)
                .addValue("status", status);
        return jdbcTemplate.query(GET_BY_QUEUE_SEQ_AND_STATUS, param, new MessageRowMapper());
    }

    @Override
    public List<MBQMessage> pull(String queueName, List<String> ids) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("queue", queueName)
                .addValue("ids", ids);
        return jdbcTemplate.query(GET_BY_QUEUE_AND_IDS, param, new MessageRowMapper());
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        List<MBQMessage> mbqMessages = createMessages(messages, queueName);
        SqlParameterSource[] params = new SqlParameterSource[messages.size()];

        for (int i = 0; i < mbqMessages.size(); i++) {
            MBQMessage msg = mbqMessages.get(i);
            params[i] = new MapSqlParameterSource()
                    .addValue("id", msg.getId())
                    .addValue("queue", msg.getQueue())
                    .addValue("status", msg.getStatus().name())
                    .addValue("data", msg.getMessage())
                    .addValue("createTS", msg.getCreatedTimeStamp())
                    .addValue("updatedTS", msg.getUpdatedTimeStamp());
        }
        jdbcTemplate.batchUpdate(INSERT_MBQ_MESSAGE, params);
        return mbqMessages;
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        return false;
    }

    private static class MessageRowMapper implements RowMapper<MBQMessage> {
        @Override
        public MBQMessage mapRow(ResultSet rs, int i) throws SQLException {
            String id = rs.getString("Id");
            String queue = rs.getString("QueueName");
            String seq = rs.getString("Sequence");
            String status = rs.getString("Status");
            String data = rs.getString("Data");
            long createTS = rs.getTimestamp("CreatedTime").getTime();
            long updatedTS = rs.getTimestamp("UpdatedTime").getTime();
            return new MBQMessage(id, queue, seq, QueueStatus.valueOf(status), data, createTS, updatedTS);
        }
    }
}
