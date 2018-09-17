package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.web.response.BeanBody;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.util.ClasspathResources;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(request.pathParam("id")).thenReturn("1");

        var builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("get", Integer.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("webservice-test/test-webservice-controller-get.java"));

        Response response = controller.execute(request);
        assertThat(response.status()).isEqualTo(HTTPStatus.OK);
        @SuppressWarnings("unchecked")
        var bean = (Optional<TestWebService.TestResponse>) ((BeanBody) ((ResponseImpl) response).body).bean;
        assertThat(bean.orElseThrow().intField).isEqualTo(2);
    }

    @Test
    void create() throws Exception {
        var requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.pathParam("id")).thenReturn("1");
        when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        var builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("create", Integer.class, TestWebService.TestRequest.class));
        Controller controller = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("webservice-test/test-webservice-controller-create.java"));

        Response response = controller.execute(request);
        assertThat(response.status()).isEqualTo(HTTPStatus.CREATED);
    }

    @Test
    void patch() throws Exception {
        var requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.pathParam("id")).thenReturn("1");
        when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        var builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("patch", Integer.class, TestWebService.TestRequest.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("webservice-test/test-webservice-controller-patch.java"));

        Response response = controller.execute(request);
        assertThat(response.status()).isEqualTo(HTTPStatus.OK);
    }

    @Test
    void testGetEnum() throws Exception {
        when(request.pathParam("id")).thenReturn("1");
        when(request.pathParam("enum")).thenReturn("A1");

        var builder = new WebServiceControllerBuilder<>(TestWebService.class, serviceImpl, TestWebService.class.getDeclaredMethod("getEnum", Long.class, TestWebService.TestEnum.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("webservice-test/test-webservice-controller-getEnum.java"));

        Response response = controller.execute(request);
        assertThat(response.status()).isEqualTo(HTTPStatus.OK);

        var bean = (TestWebService.TestResponse) ((BeanBody) ((ResponseImpl) response).body).bean;
        assertThat(bean).isNotNull();
    }

    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public TestResponse search(TestSearchRequest request) {
            return null;
        }

        @Override
        public Optional<TestResponse> get(Integer id) {
            assertThat(id).isEqualTo(1);

            TestResponse response = new TestResponse();
            response.intField = 2;
            return Optional.of(response);
        }

        @Override
        public void create(Integer id, TestRequest request) {
            assertThat(id).isEqualTo(1);
            assertThat(request.stringField).isEqualTo("value");
        }

        @Override
        public void delete(String id) {

        }

        @Override
        public void patch(Integer id, TestRequest request) {
            assertThat(id).isEqualTo(1);
            assertThat(request.stringField).isEqualTo("value");
        }

        @Override
        public TestResponse getEnum(Long id, TestEnum enumValue) {
            assertThat(id).isEqualTo(1);
            assertThat(enumValue).isEqualTo(TestEnum.A);
            return new TestResponse();
        }
    }
}
