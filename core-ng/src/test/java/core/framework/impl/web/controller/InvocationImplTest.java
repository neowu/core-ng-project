package core.framework.impl.web.controller;

import core.framework.api.http.HTTPStatus;
import core.framework.web.Controller;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class InvocationImplTest {
    @Test
    void process() throws Exception {
        Stack stack = new Stack();
        TestController controller = new TestController(stack, 3);
        Interceptors interceptors = new Interceptors();
        interceptors.add(new TestInterceptor(stack, 0));
        interceptors.add(new TestInterceptor(stack, 1));
        interceptors.add(new TestInterceptor(stack, 2));
        InvocationImpl invocation = new InvocationImpl(new ControllerHolder(controller, null, null, null, false), interceptors, mock(Request.class), new WebContextImpl());

        Response response = invocation.proceed();
        assertEquals(HTTPStatus.NO_CONTENT, response.status());
        assertTrue(controller.executed);
        for (Interceptor interceptor : interceptors.interceptors) {
            assertTrue(((TestInterceptor) interceptor).executed);
        }
    }

    @Test
    void skipInterceptor() throws Exception {
        Stack stack = new Stack();
        TestController controller = new TestController(stack, 0);
        Interceptors interceptors = new Interceptors();
        interceptors.add(new TestInterceptor(stack, 0));
        InvocationImpl invocation = new InvocationImpl(new ControllerHolder(controller, null, null, null, true), interceptors, mock(Request.class), new WebContextImpl());

        Response response = invocation.proceed();
        assertEquals(HTTPStatus.NO_CONTENT, response.status());
        assertTrue(controller.executed);
        for (Interceptor interceptor : interceptors.interceptors) {
            assertFalse(((TestInterceptor) interceptor).executed);
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
