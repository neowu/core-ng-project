package core.framework.impl.web.bean;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
public class QueryParamBeanTypeValidatorTest {
    @Test
    public void beanType() {
        new QueryParamBeanTypeValidator(TestQueryParamBean.class).validate();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestQueryParamBean {
        @XmlElement(name = "int_field")
        public Integer intField;

        @XmlElement(name = "string_field")
        public String stringField;
    }
}
