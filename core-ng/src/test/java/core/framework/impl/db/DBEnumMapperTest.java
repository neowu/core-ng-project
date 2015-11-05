package core.framework.impl.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class DBEnumMapperTest {
    private DBEnumMapper<TestEnum> mapper;

    @Before
    public void createDBEnumMapper() {
        mapper = new DBEnumMapper<>(TestEnum.class);
    }

    @Test
    public void getEnum() {
        Assert.assertNull(mapper.getEnum(null));
        Assert.assertEquals(TestEnum.V1, mapper.getEnum("DB_V1"));
        Assert.assertEquals(TestEnum.V2, mapper.getEnum("DB_V2"));
    }
}