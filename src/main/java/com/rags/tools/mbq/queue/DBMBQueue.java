package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            DataSource ds = createDS(config);
            jdbcTemplate = new NamedParameterJdbcTemplate(ds);
        } catch (Exception e) {
            throw new MBQException("Error in configuring connection pool with RDB", e);
        }
    }

    private DataSource createDS(QConfig.ServerConfig config) {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUser());
        ds.setPassword(config.getPassword());
        ds.setInitialSize(1);
        ds.setDriverClassName(config.getDbDriver());
        ds.setValidationQuery(config.getValidationQuery());
        ds.setMaxTotal(config.getMaxConn());
        return ds;
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
        List<String> qStatus = status.stream().map(Enum::name).collect(Collectors.toList());
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("queue", queueName)
                .addValue("seq", seqKey)
                .addValue("status", qStatus);
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
