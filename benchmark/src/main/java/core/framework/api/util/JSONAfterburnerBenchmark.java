package core.framework.api.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import core.framework.api.util.json.UpdateProductRequest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class JSONAfterburnerBenchmark {
    private final byte[] json = Strings.bytes("{\"request_id\":\"d2bbf14c-aff0-402d-80a6-ed8a672ab231\",\"product\":{\"vendor_number\":\"V00090\",\"vendor_sku\":\"ELP-430205-C\",\"listing_status\":\"NOT_LIVE\",\"parent_vendor_sku\":\"ELP-430205-C\",\"name\":null,\"description\":null,\"brand_name\":null,\"category_id\":null,\"external_product_id_type\":null,\"external_product_id\":null,\"type\":null,\"unit_cost\":null,\"list_price\":null,\"map_price\":null,\"standard_price\":null,\"sale_price\":null,\"sale_start_date\":null,\"sale_end_date\":null,\"weight\":null,\"harmonized_code\":null,\"on_hold\":null,\"attributes\":{},\"eligible_country_codes\":null,\"images\":[],\"inventories\":[{\"warehouse_code\":\"V00090A\",\"quantity\":0}]},\"requested_by\":\"channel-advisor\",\"result_queue\":\"channel-advisor-service-queue\"}");
    private ObjectMapper objectMapper;
    private ObjectMapper objectMapperWithAfterburner;

    @Setup
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new ISO8601DateFormat());
        objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);

        objectMapperWithAfterburner = new ObjectMapper();
        objectMapperWithAfterburner.registerModule(new JavaTimeModule());
        objectMapperWithAfterburner.registerModule(new AfterburnerModule());
        objectMapperWithAfterburner.setDateFormat(new ISO8601DateFormat());
        objectMapperWithAfterburner.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        objectMapperWithAfterburner.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapperWithAfterburner.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
    }

    @Benchmark
    public void fromJSONBytesWithAfterburner() throws IOException {
        objectMapperWithAfterburner.readValue(json, UpdateProductRequest.class);
    }

    @Benchmark
    public void fromJSONBytes() throws IOException {
        objectMapper.readValue(json, UpdateProductRequest.class);
    }
}
