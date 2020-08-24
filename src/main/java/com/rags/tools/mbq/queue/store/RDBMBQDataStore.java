package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RDBMBQDataStore extends AbstractMBQDataStore {

    private static final String GET_BY_IDS = "select * from MBQueueMessage where Id in (:ids)";
    private static final String GET_BY_QUEUE_SEQ_AND_STATUS = "select * from MBQueueMessage where QueueName=:queue and Sequence=:seq and Status in (:status)";
    private static final String GET_PENDING_IDS = "select Id, Sequence, QueueName, Status, ScheduledAt from MBQueueMessage where Status!='COMPLETED' order by CreatedTime asc";

    private static final String INSERT_MBQ_MESSAGE = "insert into MBQueueMessage (Id, QueueName, Sequence, Status, Data, ScheduledAt, CreatedTime, UpdatedTime) values (:id,:queue,:seq,:status,:data,:scheduledAt, :createTS,:updatedTS)";
    private static final String UPDATE_MBQ_MESSAGE = "update MBQueueMessage set Status=:status, UpdatedTime=:updatedTS where Id in (:ids) and QueueName=:queue";

    private static final String UPDATE_QUEUE_STATUS_TO_NEW = "update MBQueueMessage set Status=:newStatus where Status=:prevStatus";


    private static final String SELECT_REQUEST = "select Id, QueueName, Sequence, Status, ScheduledAt, BlockerKey, CreatedTime, UpdatedTime from MBQueueMessage where ";
    private static final String WHERE_IDS = " Id in (:ids) ";
    private static final String WHERE_QUEUE_NAME = " QueueName in (:queue) ";
    private static final String WHERE_SEQUENCE = " Sequence in (:seq) ";
    private static final String WHERE_STATUS = " Status in (:status) ";

    private static RDBMBQDataStore INSTANCE;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RDBMBQDataStore(QConfig.ServerConfig config) {
        try {
            DataSource ds = createDS(config);
            jdbcTemplate = new NamedParameterJdbcTemplate(ds);
        } catch (Exception e) {
            throw new MBQException("Error in configuring connection pool with RDB", e);
        }
    }

    public static synchronized RDBMBQDataStore getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new RDBMBQDataStore(config);
        }
        return INSTANCE;
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
    public List<MBQMessage> get(String queueName, List<String> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("queue", queueName)
                .addValue("ids", ids);
        return jdbcTemplate.query(GET_BY_IDS, param, new MessageRowMapper());
    }

    @Override
    public Map<String, List<IdSeqKey>> getAllPendingIds() {
        return jdbcTemplate.query(GET_PENDING_IDS, rs -> {
            Map<String, List<IdSeqKey>> queueMap = new HashMap<>();
            while (rs.next()) {
                String id = rs.getString("Id");
                String queueName = rs.getString("QueueName");
                String seq = rs.getString("Sequence");
                QueueStatus status = QueueStatus.valueOf(rs.getString("Status"));
                long scheduledAt = rs.getTimestamp("ScheduledAt").getTime();
                if (!queueMap.containsKey(queueName)) {
                    queueMap.put(queueName, new LinkedList<>());
                }
                queueMap.get(queueName).add(new IdSeqKey(id, seq, status, scheduledAt));
            }
            return queueMap;
        });
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        if (messages.isEmpty()) {
            return new LinkedList<>();
        }

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
                    .addValue("scheduledAt", new Timestamp(msg.getScheduledAt()))
                    .addValue("createTS", new Timestamp(msg.getCreatedTimeStamp()))
                    .addValue("updatedTS", null);
        }

        jdbcTemplate.batchUpdate(INSERT_MBQ_MESSAGE, params);
        return mbqMessages;
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        if (!ids.isEmpty()) {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("ids", ids)
                    .addValue("queue", queueName)
                    .addValue("status", status.name())
                    .addValue("updatedTS", new Timestamp(System.currentTimeMillis()));
            jdbcTemplate.update(UPDATE_MBQ_MESSAGE, params);
        }
        return true;
    }

    @Override
    public boolean updateStatus(String queueName, Map<QueueStatus, List<String>> statusIds) {
        if (!statusIds.isEmpty()) {
            SqlParameterSource[] params = new SqlParameterSource[statusIds.size()];
            AtomicInteger counter = new AtomicInteger(0);
            statusIds.forEach((status, ids) -> params[counter.getAndIncrement()] = new MapSqlParameterSource()
                    .addValue("ids", ids)
                    .addValue("queue", queueName)
                    .addValue("status", status.name())
                    .addValue("updatedTS", new Timestamp(System.currentTimeMillis())));

            jdbcTemplate.batchUpdate(UPDATE_MBQ_MESSAGE, params);
        }
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

        private final boolean withMessage;

        public MessageRowMapper() {
            this.withMessage = true;
        }

        public MessageRowMapper(boolean withMessage) {
            this.withMessage = withMessage;
        }

        @Override
        public MBQMessage mapRow(ResultSet rs, int i) throws SQLException {
            String id = rs.getString("Id");
            String queue = rs.getString("QueueName");
            String seq = rs.getString("Sequence");
            String status = rs.getString("Status");

            String data = null;
            if (withMessage) {
                data = rs.getString("Data");
            }
            long createTS = rs.getTimestamp("CreatedTime").getTime();
            long updatedTS = rs.getTimestamp("UpdatedTime") == null ? 0 : rs.getTimestamp("UpdatedTime").getTime();
            return new MBQMessage(id, queue, seq, QueueStatus.valueOf(status), data == null ? null : data.getBytes(), createTS, updatedTS, 0);
        }
    }

    @Override
    public List<MBQMessage> search(SearchRequest req) {
        if (req.isInValid()) {
            return Collections.emptyList();
        }

        String sql = SELECT_REQUEST;
        MapSqlParameterSource param = new MapSqlParameterSource();
        if (req.getIds() != null && !req.getIds().isEmpty()) {
            sql += WHERE_IDS;
            param.addValue("ids", req.getIds());
        }

        if (req.getQueues() != null && !req.getQueues().isEmpty()) {
            sql += WHERE_QUEUE_NAME;
            param.addValue("queue", req.getQueues());
        }

        if (req.getStatus() != null) {
            sql += WHERE_STATUS;
            param.addValue("status", req.getStatus().name());
        }

        if (req.getSequence() != null && !req.getSequence().trim().isEmpty()) {
            sql += WHERE_SEQUENCE;
            param.addValue("seq", req.getSequence().trim());
        }

        return jdbcTemplate.query(sql, param, new MessageRowMapper(false));
    }
}
