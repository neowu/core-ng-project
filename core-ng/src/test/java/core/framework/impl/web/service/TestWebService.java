package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.ResponseStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author neo
 */
public interface TestWebService {
    @GET
    @Path("/test")
    TestResponse search(TestSearchRequest request);

    @GET
    @Path("/test/:id")
    TestResponse get(@PathParam("id") Integer id);

    @PUT
    @Path("/test/:id")
    @ResponseStatus(HTTPStatus.CREATED)
    void create(@PathParam("id") Integer id, TestRequest request);

    @DELETE
    @Path("/test/:id")
    void delete(@PathParam("id") String id);

    @PUT
    @Path("/test")
    List<TestResponse> batch(List<TestRequest> requests);

    @XmlAccessorType(XmlAccessType.FIELD)
    class TestRequest {
        @XmlElement(name = "string_field")
        public String stringField;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    class TestSearchRequest {
        @XmlElement(name = "int_field")
        public Integer intField;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    class TestResponse {
        @XmlElement(name = "int_field")
        public Integer intField;
    }
}
