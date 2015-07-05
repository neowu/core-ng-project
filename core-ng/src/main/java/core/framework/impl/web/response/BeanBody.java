package core.framework.impl.web.response;

/**
 * @author neo
 */
public class BeanBody implements Body {
    public final Object bean;

    public BeanBody(Object bean) {
        this.bean = bean;
    }
}
