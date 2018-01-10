package core.framework.impl.web.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
class WebServiceImplValidatorTest {
    @Test
    void validate() {
        new WebServiceImplValidator<>(TestWebService.class, new TestWebServiceImpl()).validate();
    }

    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public TestResponse search(TestSearchRequest request) {
            return null;
        }

        @Override
        public Optional<TestResponse> get(Integer id) {
            return Optional.empty();
        }

        @Override
        public void create(Integer id, TestRequest request) {
        }

        @Override
        public void delete(String id) {
        }

        @Override
        public List<TestResponse> batch(List<TestRequest> requests) {
            return null;
        }

        @Override
        public void patch(Integer id, TestRequest request) {

        }
    }
}
