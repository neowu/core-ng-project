package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.json.Property;
import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.PATCH;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.api.web.service.ResponseStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public interface TestWebService {
    @GET
    @Path("/test")
    TestResponse search(TestSearchRequest request);

    @GET
    @Path("/test/:id")
    Optional<TestResponse> get(@PathParam("id") Integer id);

    @PUT
    @Path("/test/:id")
    @ResponseStatus(HTTPStatus.CREATED)
    void create(@PathParam("id") Integer id, TestRequest request);

    @DELETE
    @Path("/test/:id")
    void delete(@PathParam("id") String id);

    @PATCH
    @Path("/test/:id")
    void patch(@PathParam("id") Integer id, TestRequest request);

    @GET
    @Path("/test/:id/:enum")
    TestResponse getEnum(@PathParam("id") Long id, @PathParam("enum") TestEnum enumValue);

    enum TestEnum {
        @Property(name = "A1")
        A,
        @Property(name = "B1")
        B
    }

    class TestRequest {
        @NotNull
        @Pattern("\\d+.*")
        @Length(max = 10)
        @Property(name = "string_field")
        public String stringField;

        @Property(name = "items")
        public List<TestItem> items;
    }

    class TestItem {
        @Property(name = "zoned_date_time_field")
        public ZonedDateTime zonedDateTimeField;

        @Property(name = "enum_field")
        public TestEnum enumField;
    }

    class TestSearchRequest {
        @NotNull
        @QueryParam(name = "int_field")
        public Integer intField;

        @QueryParam(name = "boolean_field")
        public Boolean booleanField;

        @QueryParam(name = "long_field")
        public Long longField;

        @Min(1)
        @Max(100)
        @QueryParam(name = "double_field")
        public Long doubleField;

        @QueryParam(name = "date_field")
        public LocalDate dateField;
    }

    class TestResponse {
        @NotNull
        @Property(name = "int_field")
        public Integer intField;

        @Size(min = 1)
        @Property(name = "string_map")
        public Map<String, String> stringMap;

        @Property(name = "items")
        public Map<String, TestItem> items;
    }
}
