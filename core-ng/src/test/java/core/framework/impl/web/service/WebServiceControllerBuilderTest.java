package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.web.response.BeanBody;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Types;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class WebServiceControllerBuilderTest {
    private Request request;
    private TestWebServiceImpl serviceImpl;

    @BeforeEach
    void prepare() {
        serviceImpl = new TestWebServiceImpl();
        request = Mockito.mock(Request.class);
    }

    @Test
    void get() throws Exception {
        when(request.pathParam("id", Integer.class)).thenReturn(1);

        WebServiceControllerBuilder<TestWebService> builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("get", Integer.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-controller-get.java"), sourceCode);

        Response response = controller.execute(request);
        assertEquals(HTTPStatus.OK, response.status());
        @SuppressWarnings("unchecked")
        Optional<TestWebService.TestResponse> bean = (Optional<TestWebService.TestResponse>) ((BeanBody) ((ResponseImpl) response).body).bean;
        assertEquals(2, (int) bean.get().intField);
    }

    @Test
    void create() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.pathParam("id", Integer.class)).thenReturn(1);
        when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        WebServiceControllerBuilder<TestWebService> builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("create", Integer.class, TestWebService.TestRequest.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-controller-create.java"), sourceCode);

        Response response = controller.execute(request);
        assertEquals(HTTPStatus.CREATED, response.status());
    }

    @Test
    void batch() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.bean(Types.list(TestWebService.TestRequest.class))).thenReturn(Lists.newArrayList(requestBean));

        Controller controller = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("batch", List.class)).build();
        Response response = controller.execute(request);
        assertEquals(HTTPStatus.OK, response.status());
    }

    @Test
    void patch() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.pathParam("id", Integer.class)).thenReturn(1);
        when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        WebServiceControllerBuilder<TestWebService> builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("patch", Integer.class, TestWebService.TestRequest.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-controller-patch.java"), sourceCode);

        Response response = controller.execute(request);
        assertEquals(HTTPStatus.OK, response.status());
    }

    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public TestResponse search(TestSearchRequest request) {
            return null;
        }

        @Override
        public Optional<TestResponse> get(Integer id) {
            assertEquals(1, (int) id);

            TestResponse response = new TestResponse();
            response.intField = 2;
            return Optional.of(response);
        }

        @Override
        public void create(Integer id, TestRequest request) {
            assertEquals(1, (int) id);
            assertEquals("value", request.stringField);
        }

        @Override
        public void delete(String id) {

        }

        @Override
        public List<TestResponse> batch(List<TestRequest> requests) {
            assertEquals(1, requests.size());
            return Lists.newArrayList();
        }

        @Override
        public void patch(Integer id, TestRequest request) {
            assertEquals(1, (int) id);
            assertEquals("value", request.stringField);
        }
    }
}
