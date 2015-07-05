package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author neo
 */
public class YAMLTest {
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Bean {
        @XmlElement(name = "id")
        public Integer id;
        @XmlElement(name = "name")
        public String name;
    }

    @Test
    public void load() {
        Bean bean = YAML.load(Bean.class, ClasspathResources.text("yaml-test/object.yml"));
        Assert.assertEquals(1, (int) bean.id);
        Assert.assertEquals("name1", bean.name);
    }

    @Test
    public void loadList() {
        List<Bean> beans = YAML.loadList(Bean.class, ClasspathResources.text("yaml-test/list.yml"));

        Assert.assertEquals(2, beans.size());
        Assert.assertEquals(1, (int) beans.get(0).id);
        Assert.assertEquals("name1", beans.get(0).name);
        Assert.assertEquals(2, (int) beans.get(1).id);
        Assert.assertEquals("name2", beans.get(1).name);
    }
}