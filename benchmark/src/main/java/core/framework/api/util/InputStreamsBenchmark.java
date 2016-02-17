package core.framework.api.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class InputStreamsBenchmark {
    private static byte[] bytesOldVersion(InputStream stream, int initialCapacity) {
        byte[] bytes = new byte[initialCapacity];
        int position = 0;
        try {
            while (true) {
                int bytesToRead = bytes.length - position;
                int bytesRead = stream.read(bytes, position, bytesToRead);
                if (bytesRead < 0) break;
                position += bytesRead;
                if (position >= bytes.length) {
                    byte[] newBytes = new byte[bytes.length * 2];
                    System.arraycopy(bytes, 0, newBytes, 0, position);
                    bytes = newBytes;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] result = new byte[position];
        System.arraycopy(bytes, 0, result, 0, position);
        return result;
    }

    private final byte[] content = Strings.bytes("{\"request_id\":\"d2bbf14c-aff0-402d-80a6-ed8a672ab231\",\"product\":{\"vendor_number\":\"V00090\",\"vendor_sku\":\"ELP-430205-C\",\"listing_status\":\"NOT_LIVE\",\"parent_vendor_sku\":\"ELP-430205-C\",\"name\":null,\"description\":null,\"brand_name\":null,\"category_id\":null,\"external_product_id_type\":null,\"external_product_id\":null,\"type\":null,\"unit_cost\":null,\"list_price\":null,\"map_price\":null,\"standard_price\":null,\"sale_price\":null,\"sale_start_date\":null,\"sale_end_date\":null,\"weight\":null,\"harmonized_code\":null,\"on_hold\":null,\"attributes\":{},\"eligible_country_codes\":null,\"images\":[],\"inventories\":[{\"warehouse_code\":\"V00090A\",\"quantity\":0}]},\"requested_by\":\"channel-advisor\",\"result_queue\":\"channel-advisor-service-queue\"}");

    @Benchmark
    public void oldVersion() throws IOException {
        try (InputStream stream = new ByteArrayInputStream(content)) {
            bytesOldVersion(stream, 32);
        }
    }

    @Benchmark
    public void current() throws IOException {
        try (InputStream stream = new ByteArrayInputStream(content)) {
            InputStreams.bytes(stream, 32);
        }
    }
}
