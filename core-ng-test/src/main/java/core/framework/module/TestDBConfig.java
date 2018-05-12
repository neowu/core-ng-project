package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.util.Strings;

/**
 * @author neo
 */
public class TestDBConfig extends DBConfig {
    TestDBConfig(ModuleContext context, String name) {
        super(context, name);
    }

    @Override
    void setDatabaseURL(String url) {
        String syntaxParam = hsqldbSyntaxParam();
        database.url(Strings.format("jdbc:hsqldb:mem:{};{}", name == null ? "." : name, syntaxParam));
    }

    private String hsqldbSyntaxParam() {
        switch (database.vendor) {
            case ORACLE:
                return "sql.syntax_ora=true";
            case MYSQL:
                return "sql.syntax_mys=true";
            default:
                return "";
        }
    }

    @Override
    public void user(String user) {
    }

    @Override
    public void password(String password) {
    }
}
