package core.framework.impl.web.service;

import core.framework.api.web.service.GET;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.impl.web.bean.BeanMapperRegistry;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class WebServiceInterfaceValidatorTest {
    @Test
    void validate() {
        var validator = validator(TestWebService.class);
        validator.validate();
    }

    @Test
    void validateRequestBeanClass() throws NoSuchMethodException {
        var validator = validator(TestWebService.class);
        var method = TestWebService.class.getDeclaredMethod("get", Integer.class);

        assertThatThrownBy(() -> validator.validateRequestBeanClass(Integer.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("if it is path param, please add @PathParam");

        assertThatThrownBy(() -> validator.validateRequestBeanClass(TestEnum.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("if it is path param, please add @PathParam");

        assertThatThrownBy(() -> validator.validateRequestBeanClass(Map.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("request bean type must be bean class");
    }

    @Test
    void validateResponseBeanType() throws NoSuchMethodException {
        var validator = validator(TestWebService.class);
        var method = TestWebService.class.getDeclaredMethod("get", Integer.class);

        assertThatThrownBy(() -> validator.validateResponseBeanType(Integer.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class or Optional<T>");

        assertThatThrownBy(() -> validator.validateResponseBeanType(Types.map(String.class, String.class), method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class or Optional<T>");
    }

    @Test
    void duplicateMethodNames() {
        var validator = validator(WebServiceWithDuplicateMethod.class);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(Error.class).hasMessageContaining("found duplicate method name");
    }

    private WebServiceInterfaceValidator validator(Class<?> serviceInterface) {
        var registry = new BeanMapperRegistry();
        return new WebServiceInterfaceValidator(serviceInterface, new RequestBeanMapper(registry), new ResponseBeanMapper(registry));
    }

    enum TestEnum {
        A;
    }

    interface WebServiceWithDuplicateMethod {
        @GET
        @Path("/test/:id")
        void get(@PathParam("id") Integer id);

        @GET
        @Path("/test/:id1/:id2")
        void get(@PathParam("id1") String id1, @PathParam("id2") String id2);
    }
}
