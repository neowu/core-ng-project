package core.framework.api.util;

import core.framework.api.util.json.UpdateProductRequest;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class JSONBenchmark {
    private static final String JSON_STRING = "{\"request_id\":\"d2bbf14c-aff0-402d-80a6-ed8a672ab231\",\"product\":{\"vendor_number\":\"V00090\",\"vendor_sku\":\"ELP-430205-C\",\"listing_status\":\"NOT_LIVE\",\"parent_vendor_sku\":\"ELP-430205-C\",\"name\":null,\"description\":null,\"brand_name\":null,\"category_id\":null,\"external_product_id_type\":null,\"external_product_id\":null,\"type\":null,\"unit_cost\":null,\"list_price\":null,\"map_price\":null,\"standard_price\":null,\"sale_price\":null,\"sale_start_date\":null,\"sale_end_date\":null,\"weight\":null,\"harmonized_code\":null,\"on_hold\":null,\"attributes\":{},\"eligible_country_codes\":null,\"images\":[],\"inventories\":[{\"warehouse_code\":\"V00090A\",\"quantity\":0}]},\"requested_by\":\"channel-advisor\",\"result_queue\":\"channel-advisor-service-queue\"}";
    private byte[] jsonBytes;
    private UpdateProductRequest object;
    private JSONReader<UpdateProductRequest> reader;
    private JSONWriter<UpdateProductRequest> writer;

    @Setup
    public void setup() {
        jsonBytes = Strings.bytes(JSON_STRING);
        object = JSON.fromJSON(UpdateProductRequest.class, JSON_STRING);
        reader = JSONReader.of(UpdateProductRequest.class);
        writer = JSONWriter.of(UpdateProductRequest.class);
    }

    @Benchmark
    public void toJSONBytes() {
        writer.toJSON(object);
    }

    @Benchmark
    public void toJSONBytesWithMapper() {
        JSONMapper.toJSON(object);
    }

    @Benchmark
    public void toJSONString() {
        JSON.toJSON(object);
    }

    @Benchmark
    public void fromJSONString() {
        JSON.fromJSON(UpdateProductRequest.class, JSON_STRING);
    }

    @Benchmark
    public void fromJSONBytes() {
        reader.fromJSON(jsonBytes);
    }

    @Benchmark
    public void fromJSONBytesWithMapper() {
        JSONMapper.fromJSON(UpdateProductRequest.class, jsonBytes);
    }
}
