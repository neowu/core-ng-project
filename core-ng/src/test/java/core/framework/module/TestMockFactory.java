package core.framework.module;

import core.framework.impl.module.MockFactory;
import org.mockito.Mockito;

/**
 * @author neo
 */
class TestMockFactory implements MockFactory {
    @Override
    public <T> T create(Class<T> instanceClass, Object... params) {
        return Mockito.mock(instanceClass);
    }
}
