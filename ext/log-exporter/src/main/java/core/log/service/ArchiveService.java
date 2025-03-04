package core.log.service;

import core.framework.crypto.Hash;
import core.framework.inject.Inject;
import core.framework.util.Files;
import core.framework.util.Network;
import core.framework.util.Randoms;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.conf.PlainParquetConfiguration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.LocalOutputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * @author neo
 */
public class ArchiveService {
    private final Logger logger = LoggerFactory.getLogger(ArchiveService.class);
    private final String hash = Hash.md5Hex(Network.LOCAL_HOST_NAME).substring(0, 5);   // generally there only need one log-exporter, this is to avoid file name collision with multiple log-exporter
    private final Shell shell = new Shell();

    public Path logDir = Path.of("/var/log/app");

    @Inject
    UploadService uploadService;

    public void uploadArchive(LocalDate date) throws IOException {
        logger.info("uploading begin, date={}", date);
        upload(localActionLogFilePath(date), remoteActionLogPath(date));
        upload(localEventFilePath(date), remoteEventPath(date));
        logger.info("uploading end, date={}", date);
    }

    private void upload(Path localFilePath, String remotePath) throws IOException {
        if (exists(localFilePath)) {
            Path parquetFilePath = convertToParquet(localFilePath);
            uploadService.upload(parquetFilePath, remotePath);
            Files.delete(parquetFilePath);
        }
    }

    Path convertToParquet(Path sourcePath) throws IOException {
        var watch = new StopWatch();
        var targetPath = sourcePath.resolveSibling(sourcePath.getFileName() + "." + Randoms.alphaNumeric(5) + ".parquet");
        var config = new PlainParquetConfiguration();
        config.setBoolean("parquet.avro.write-old-list-structure", false);
        try (DataFileReader<GenericData.Record> reader = new DataFileReader<>(sourcePath.toFile(), new GenericDatumReader<>());
             ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                 .<GenericData.Record>builder(new LocalOutputFile(targetPath))
                 .withSchema(reader.getSchema())
                 .withCompressionCodec(CompressionCodecName.ZSTD)
                 .withConf(config)
                 .build()) {

            for (GenericData.Record record : reader) {
                writer.write(record);
            }

        } finally {
            logger.info("convert avro to parquet, source={}, target={}, elapsed={}", sourcePath, targetPath, watch.elapsed());
        }
        return targetPath;
    }

    public void cleanupArchive(LocalDate date) {
        logger.info("cleaning up archives, date={}", date);

        Path actionLogFilePath = localActionLogFilePath(date);
        shell.execute("rm", "-f", actionLogFilePath.toString());

        Path eventFilePath = localEventFilePath(date);
        shell.execute("rm", "-f", eventFilePath.toString());
    }

    String remoteActionLogPath(LocalDate date) {
        return Strings.format("/action/{}/action-{}-{}.parquet", date.getYear(), date, hash);
    }

    String remoteEventPath(LocalDate date) {
        return Strings.format("/event/{}/event-{}-{}.parquet", date.getYear(), date, hash);
    }

    public Path localActionLogFilePath(LocalDate date) {
        String path = Strings.format("/action/{}/action-{}-{}.avro", date.getYear(), date, hash);
        return Path.of(logDir.toString(), path);
    }

    public Path localEventFilePath(LocalDate date) {
        String path = Strings.format("/event/{}/event-{}-{}.avro", date.getYear(), date, hash);
        return Path.of(logDir.toString(), path);
    }

    public void createParentDir(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !exists(parent)) createDirectories(parent);
    }
}
