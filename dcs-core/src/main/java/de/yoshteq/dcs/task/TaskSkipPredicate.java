package de.yoshteq.dcs.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.DcsRuntimeException;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.SkipPredicate;
import io.quarkus.scheduler.ScheduledExecution;

@ApplicationScoped
public class TaskSkipPredicate implements SkipPredicate
{
	private static final Logger logger = LoggerFactory.getLogger(TaskSkipPredicate.class);

	@Inject
	DataSource datasource;

	@Inject
	@ConfigProperty(name = "quarkus.datasource.db-kind")
	String dbKind;

	private String nodeUuid = UUID.randomUUID().toString();

	private Set<String> taskIdentities = Collections.synchronizedSet(new HashSet<>());

	private Map<String, Boolean> taskHandlerMap = new HashMap<>();

	@Override
	public boolean test(ScheduledExecution execution)
	{
		var taskId = execution.getTrigger().getId();
		if (!taskIdentities.contains(taskId))
		{
			taskIdentities.add(taskId);
			checkTaskNode();
		}
		return !taskHandlerMap.getOrDefault(taskId, false);
	}

	@Scheduled(every = "30s")
	public void checkTaskNode()
	{
		try (var connection = datasource.getConnection())
		{
			connection.setAutoCommit(false);

			try (var lockStatement = connection.createStatement();
				var unlockStatement = connection.createStatement();
				var queryStatement = connection.createStatement();)
			{
				if (dbKind.equalsIgnoreCase("mssql"))
				{
					lockStatement.execute("SELECT 1 FROM task_node WITH (TABLOCKX)");
				}
				else if (dbKind.equalsIgnoreCase("mariadb"))
				{
					lockStatement.execute("LOCK TABLES task_node WRITE");
				}
				else if (dbKind.equalsIgnoreCase("postgresql "))
				{
					lockStatement.execute("LOCK TABLE task_node IN EXCLUSIVE MODE");
				}
				else
				{
					throw new DcsRuntimeException("Unsupported database kind: " + dbKind);
				}

				var identities = new HashSet<>(taskIdentities);
				var resultSet = queryStatement.executeQuery("SELECT task_id, node_uuid, last_liveness_datetime FROM task_node");
				while (resultSet.next())
				{
					var taskId = resultSet.getString(1);
					if (identities.contains(taskId))
					{
						determineTaskIdHandler(connection, resultSet, taskId);
						identities.remove(taskId);
					}
				}

				for (var taskId : identities)
				{
					insertTaskNodeEntry(taskId, connection);
				}

				connection.commit();
				if (dbKind.equalsIgnoreCase("mariadb"))
				{
					unlockStatement.execute("UNLOCK TABLES");
				}
			}

		}
		catch (SQLException e)
		{
			logger.error("Could not determine task handler", e);
		}
	}

	private void determineTaskIdHandler(Connection connection, ResultSet resultSet, String taskId) throws SQLException
	{
		var taskNodeUuid = resultSet.getString(2);
		var lastLivenessCheck = resultSet.getTimestamp(3);
		if (nodeUuid.equals(taskNodeUuid) || lastLivenessCheck.before(new Timestamp(System.currentTimeMillis() - 40 * 1000)))
		{
			logger.debug("This node handles tasks with id {}", taskId);
			taskHandlerMap.put(taskId, true);
			updateTaskNodeEntry(taskId, connection);
		}
		else
		{
			taskHandlerMap.put(taskId, false);
			logger.debug("Another node handles tasks with id {}", taskId);
		}
	}

	private void insertTaskNodeEntry(String taskId, Connection connection) throws SQLException
	{
		try (
			var insertStmt = connection.prepareStatement("INSERT INTO task_node (task_id, node_uuid, last_liveness_datetime) VALUES (?, ?, ?)"))
		{
			insertStmt.setString(1, taskId);
			insertStmt.setString(2, nodeUuid);
			insertStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

			insertStmt.executeUpdate();
		}
	}

	private void updateTaskNodeEntry(String taskId, Connection connection) throws SQLException
	{
		try (var updateStmt = connection.prepareStatement("UPDATE task_node SET node_uuid = ?, last_liveness_datetime = ? WHERE task_id = ?"))
		{
			updateStmt.setString(1, nodeUuid);
			updateStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			updateStmt.setString(3, taskId);

			updateStmt.executeUpdate();
		}
	}
}
