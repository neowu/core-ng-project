package core.framework.internal.web.controller;

import core.framework.api.http.HTTPStatus;
import core.framework.web.Controller;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class InvocationImplTest {
    @Mock
    Request request;

    @Test
    void process() throws Exception {
        var stack = new Stack();
        var controller = new TestController(stack, 3);
        Interceptor[] interceptors = {new TestInterceptor(stack, 0), new TestInterceptor(stack, 1), new TestInterceptor(stack, 2)};
        var invocation = new InvocationImpl(new ControllerHolder(controller, TestController.class.getMethod("execute", Request.class), null, null, false), interceptors, request, new WebContextImpl());

        Response response = invocation.proceed();
        assertThat(response.status()).isEqualTo(HTTPStatus.NO_CONTENT);
        assertThat(controller.executed).isTrue();
        for (Interceptor interceptor : interceptors) {
            assertThat(((TestInterceptor) interceptor).executed).isTrue();
        }
    }

    @Test
    void skipInterceptor() throws Exception {
        var stack = new Stack();
        var controller = new TestController(stack, 0);
        Interceptor[] interceptors = {new TestInterceptor(stack, 0)};
        var invocation = new InvocationImpl(new ControllerHolder(controller, TestController.class.getMethod("execute", Request.class), null, null, true), interceptors, request, new WebContextImpl());

        Response response = invocation.proceed();
        assertThat(response.status()).isEqualTo(HTTPStatus.NO_CONTENT);
        assertThat(controller.executed).isTrue();
        for (Interceptor interceptor : interceptors) {
            assertThat(((TestInterceptor) interceptor).executed).isFalse();
        }
    }

    @Test
    void withNullResponse() throws NoSuchMethodException {
        Controller controller = request -> null;
        var invocation = new InvocationImpl(new ControllerHolder(controller, controller.getClass().getMethod("execute", Request.class), null, null, false), new Interceptor[0], request, new WebContextImpl());
        assertThatThrownBy(invocation::proceed).isInstanceOf(Error.class).hasMessageContaining("controller must not return null response");

        controller = request -> Response.empty();
        invocation = new InvocationImpl(new ControllerHolder(controller, controller.getClass().getMethod("execute", Request.class), null, null, false), new Interceptor[]{new TestNullResponseInterceptor()}, request, new WebContextImpl());
        assertThatThrownBy(invocation::proceed).isInstanceOf(Error.class).hasMessageContaining("interceptor must not return null response");
    }

    static class Stack {
        int currentStack = 0;
    }

    static final class TestController implements Controller {
        final Stack stack;
        final int expectedStack;
        boolean executed;

        TestController(Stack stack, int expectedStack) {
            this.stack = stack;
            this.expectedStack = expectedStack;
        }

        @Override
        public Response execute(Request request) {
            executed = true;
            assertThat(stack.currentStack).isEqualTo(expectedStack);
            return Response.empty();
        }
    }

    static final class TestInterceptor implements Interceptor {
        final Stack stack;
        final int expectedStack;
        boolean executed;

        TestInterceptor(Stack stack, int expectedStack) {
            this.stack = stack;
            this.expectedStack = expectedStack;
        }

        @Override
        public Response intercept(Invocation invocation) throws Exception {
            executed = true;
            assertThat(stack.currentStack).isEqualTo(expectedStack);
            stack.currentStack++;
            return invocation.proceed();
        }
    }

    static final class TestNullResponseInterceptor implements Interceptor {
        @Override
        public Response intercept(Invocation invocation) {
            return null;
        }
    }
}
