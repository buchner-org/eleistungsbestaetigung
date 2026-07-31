package de.yoshteq.dcs.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.yoshteq.dcs.DcsRuntimeException;
import de.yoshteq.dcs.logging.LogEntry;
import de.yoshteq.dcs.logging.LogEntryState;
import de.yoshteq.dcs.task.TaskSkipPredicate;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;

@ApplicationScoped
public class StatisticAggregationService
{
	private static final Logger logger = LoggerFactory.getLogger(StatisticAggregationService.class);

	@Inject
	DataSource dataSource;

	@Inject
	ObjectMapper objectMapper;

	@Scheduled(cron = "{dcs.task.statistics.aggregation.cron}", concurrentExecution = ConcurrentExecution.SKIP, skipExecutionIf = TaskSkipPredicate.class, identity = "statistics.aggregation")
	public void aggregateStatistics()
	{
		var entries = getNewLogEntries();
	}

	private List<LogEntry> getNewLogEntries()
	{
		try (var connection = dataSource.getConnection();
			var statement = connection
				.prepareStatement("SELECT * FROM process_log WHERE date_time > COALESCE((SELECT MAX(date_time) FROM statistic), '1970-01-01')");)
		{
			var result = statement.executeQuery();
			var logEntries = new ArrayList<LogEntry>();
			while (result.next())
			{
				logEntries.add(mapEntry(result));
			}

			return logEntries;
		}
		catch (SQLException e)
		{
			throw new DcsRuntimeException("Error aggregating statistics", e);
		}
	}

	private LogEntry mapEntry(ResultSet result) throws SQLException
	{
		var dateTime = result.getTimestamp("date_time").toLocalDateTime().atOffset(ZoneOffset.UTC);
		var detail = result.getString("detail");
		Map<String, Object> detailMap = Collections.emptyMap();
		if (detail != null)
		{
			try
			{
				detailMap = objectMapper.readValue(detail, Map.class);
			}
			catch (JsonProcessingException e)
			{
				logger.error("Could not deserialize details", e);
			}
		}

		return new LogEntry(
			dateTime,
			result.getInt("sender_ik"),
			result.getString("source_software_identifier"),
			result.getString("charge_item_uuid"),
			LogEntryState.valueOf(result.getString("state")),
			detailMap);
	}
}
