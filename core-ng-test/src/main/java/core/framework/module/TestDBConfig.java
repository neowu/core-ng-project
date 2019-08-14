package core.framework.module;

import core.framework.util.Strings;

/**
 * @author neo
 */
public class TestDBConfig extends DBConfig {
    @Override
    String databaseURL(String url) {
        return Strings.format("jdbc:hsqldb:mem:{};sql.syntax_mys=true", name == null ? "." : name);
    }

    @Override
    public void user(String user) {
    }

    @Override
    public void password(String password) {
    }
}
