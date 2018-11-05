package core.framework.impl.web.controller;

import core.framework.api.http.HTTPStatus;
import core.framework.web.Controller;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class InvocationImplTest {
    @Test
    void process() throws Exception {
        var stack = new Stack();
        var controller = new TestController(stack, 3);
        var interceptors = new Interceptors();
        interceptors.add(new TestInterceptor(stack, 0));
        interceptors.add(new TestInterceptor(stack, 1));
        interceptors.add(new TestInterceptor(stack, 2));
        var invocation = new InvocationImpl(new ControllerHolder(controller, null, null, null, false), interceptors, mock(Request.class), new WebContextImpl());

        Response response = invocation.proceed();
        assertThat(response.status()).isEqualTo(HTTPStatus.NO_CONTENT);
        assertThat(controller.executed).isTrue();
        for (Interceptor interceptor : interceptors.interceptors) {
            assertThat(((TestInterceptor) interceptor).executed).isTrue();
        }
    }

    @Test
    void skipInterceptor() throws Exception {
        var stack = new Stack();
        var controller = new TestController(stack, 0);
        var interceptors = new Interceptors();
        interceptors.add(new TestInterceptor(stack, 0));
        var invocation = new InvocationImpl(new ControllerHolder(controller, null, null, null, true), interceptors, mock(Request.class), new WebContextImpl());

        Response response = invocation.proceed();
        assertThat(response.status()).isEqualTo(HTTPStatus.NO_CONTENT);
        assertThat(controller.executed).isTrue();
        for (Interceptor interceptor : interceptors.interceptors) {
            assertThat(((TestInterceptor) interceptor).executed).isFalse();
        }
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
            assertEquals(expectedStack, stack.currentStack);
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
            assertEquals(expectedStack, stack.currentStack);
            stack.currentStack++;
            return invocation.proceed();
        }
    }
}
