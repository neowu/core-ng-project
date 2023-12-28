package app.monitor.job;

import com.mongodb.client.MongoClient;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.Severity;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class MongoMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(MongoMonitorJob.class);
    private final MongoClient mongo;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;
    public double highDiskUsageThreshold;

    public MongoMonitorJob(MongoClient mongo, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.mongo = mongo;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        try {
            Stats stats = collect();
            publisher.publish(StatMessageFactory.stats(app, host, stats));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            publisher.publish(StatMessageFactory.failedToCollect(app, host, e));
        }
    }

    Stats collect() {
        int objects = 0;
        double totalSize = 0;
        double diskUsed = 0;
        double diskMax = 0;

        Iterable<String> dbs = listDB();
        for (String db : dbs) {
            // e.g. Document{{db=admin, collections=1, views=0, objects=1, avgObjSize=59.0, dataSize=59.0, storageSize=20480.0, indexes=1, indexSize=20480.0, totalSize=40960.0, scaleFactor=1.0, fsUsedSize=1.05404416E9, fsTotalSize=5.2521566208E10, ok=1.0}}
            Document result = dbStats(db);
            objects += result.getInteger("objects");
            totalSize += result.getDouble("totalSize");
            if (diskUsed == 0) diskUsed = result.getDouble("fsUsedSize");
            if (diskMax == 0) diskMax = result.getDouble("fsTotalSize");
        }

        var stats = new Stats();
        stats.put("mongo_docs", objects);
        stats.put("mongo_total_size", totalSize);
        stats.put("mongo_disk_used", diskUsed);
        stats.put("mongo_disk_max", diskMax);
        boolean highUsage = stats.checkHighUsage(diskUsed / diskMax, highDiskUsageThreshold, "disk");
        if (highUsage) {
            stats.severity = Severity.ERROR;
        }
        return stats;
    }

    Document dbStats(String db) {
        return mongo.getDatabase(db).runCommand(new Document("dbStats", 1));
    }

    Iterable<String> listDB() {
        return mongo.listDatabaseNames();
    }
}
