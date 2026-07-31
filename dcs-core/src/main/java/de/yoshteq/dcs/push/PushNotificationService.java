package de.yoshteq.dcs.push;

import java.time.OffsetDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.logging.ProcessLogger;
import de.yoshteq.dcs.task.TaskSkipPredicate;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;

@ApplicationScoped
public class PushNotificationService
{
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

	@Inject
	EntityManager entityManager;

	@Inject
	Instance<NotificationService> notificationService;

	@Inject
	ProcessLogger processLogger;

	@Transactional
	@Scheduled(cron = "{dcs.task.push.notification.cron}", concurrentExecution = ConcurrentExecution.SKIP, skipExecutionIf = TaskSkipPredicate.class, identity = "push.notification")
	void notifyInsurantDevices()
	{
		var tasks = entityManager
			.createQuery("SELECT T FROM InsurantPushTask T WHERE T.pushDateTime IS NULL", InsurantPushTask.class)
			.getResultList();
		
		for (var task : tasks)
		{
			try
			{
				notificationService.get().sendPushNotification(task.getInsurant());
				task.setPushDateTime(OffsetDateTime.now());
				processLogger.pushedToApp(task.getInsurant().getId());
			}
			catch (Exception e)
			{
				logger.error("Could not send push notification", e);
			}
		}
		
	}

}
