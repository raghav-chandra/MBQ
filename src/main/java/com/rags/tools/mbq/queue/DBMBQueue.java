package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DBMBQueue extends AbstractMBQueue {

    private static final String GET_BY_QUEUE_AND_ID = "select * from MBQueueMessage where Id=:id";
    private static final String GET_BY_QUEUE_SEQ_AND_STATUS = "select * from MBQueueMessage where QueueName=:queue and Sequence=:seq and Status in (:status)";
    private static final String GET_BY_QUEUE_AND_IDS = "select * from MBQueueMessage where QueueName=:queue Id in (:ids)";
    private static final String GET_PENDING_IDS = "select Id, QueueName from MBQueueMessage where Status='PENDING' order by CreatedTime asc";

    private static final String INSERT_MBQ_MESSAGE = "insert into MBQueueMessage values (:id,:queue,:seq,:status,:data,:createTS,:updatedTS)";
    private static final String UPDATE_MBQ_MESSAGE = "update MBQueueMessage set Status=:status, UpdatedTime=:updatedTS where Id in (:ids) and QueueName=:queue";

    private static final String UPDATE_QUEUE_STATUS_TO_NEW = "update MBQueueMessage set Status=:newStatus where Status=:prevStatus";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DBMBQueue(QConfig.ServerConfig config) {
        try {
            /*Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/raga", "raga", "raga");*/
            Class.forName(config.getDbDriver());
            Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
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
    public Map<String, List<String>> getAllPendingIds() {
        return jdbcTemplate.query(GET_PENDING_IDS, rs -> {
            Map<String, List<String>> queueMap = new HashMap<>();
            while (rs.next()) {
                String id = rs.getString("Id");
                String queueName = rs.getString("QueueName");
                if (!queueMap.containsKey(queueName)) {
                    queueMap.put(queueName, new LinkedList<>());
                }
                queueMap.get(queueName).add(id);
            }
            return queueMap;
        });
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
                    .addValue("data", new String(msg.getMessage()))
                    .addValue("seq", msg.getSeqKey())
                    .addValue("createTS", new Timestamp(msg.getCreatedTimeStamp()))
                    .addValue("updatedTS", null);
        }

        jdbcTemplate.batchUpdate(INSERT_MBQ_MESSAGE, params);
        return mbqMessages;
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids)
                .addValue("queue", queueName)
                .addValue("status", status.name())
                .addValue("updatedTS", new Timestamp(System.currentTimeMillis()));
        jdbcTemplate.update(UPDATE_MBQ_MESSAGE, params);
        return true;
    }

    @Override
    public void updateStatus(QueueStatus prevStatus, QueueStatus newStatus) {
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("prevStatus", prevStatus.name())
                .addValue("newStatus", newStatus.name());
        jdbcTemplate.update(UPDATE_QUEUE_STATUS_TO_NEW, param);
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
            long updatedTS = rs.getTimestamp("UpdatedTime") == null ? 0 : rs.getTimestamp("UpdatedTime").getTime();
            return new MBQMessage(id, queue, seq, QueueStatus.valueOf(status), data.getBytes(), createTS, updatedTS);
        }
    }
}
