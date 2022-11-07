package core.log;

import core.framework.http.HTTPMethod;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.LogTopics;
import core.framework.module.App;
import core.log.job.ProcessLogJob;
import core.log.kafka.ActionLogMessageHandler;
import core.log.service.ArchiveService;
import core.log.service.UploadService;
import core.log.web.UploadController;
import core.log.web.UploadRequest;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class LogExporterApp extends App {
    @Override
    protected void initialize() {
        // not using SystemModule, and not put sys.log.appender property, to prevent log processor from sending its own action log to same log-kafka it's pulling from
        loadProperties("sys.properties");
        loadProperties("app.properties");

        kafka().uri(requiredProperty("sys.kafka.uri"));
        kafka().poolSize(1);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(5000));        // try to get at least 1M message, and can wait longer
        kafka().maxPoll(2000, 3 * 1024 * 1024);         // get 3M message at max

        // upload is cpu/network intensive work, queued in one thread
        executor().add(null, 1);

        bind(new UploadService(requiredProperty("app.log.bucket")));
        bind(ArchiveService.class);

        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));

        schedule().dailyAt("process-log-job", bind(ProcessLogJob.class), LocalTime.of(1, 0));

        // log-exporter will be configured as stateful set and PV, so there is no shutdown hook to upload today's file when restarting

        // manually trigger upload with date
        http().bean(UploadRequest.class);
        http().route(HTTPMethod.PUT, "/log/upload", bind(UploadController.class));
    }
}
