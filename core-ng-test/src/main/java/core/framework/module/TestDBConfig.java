package core.framework.module;

import core.framework.impl.db.Vendor;
import core.framework.util.Strings;

/**
 * @author neo
 */
public class TestDBConfig extends DBConfig {
    @Override
    String databaseURL(String url, Vendor vendor) {
        String syntaxParam = hsqldbSyntaxParam(vendor);
        return Strings.format("jdbc:hsqldb:mem:{};{}", name == null ? "." : name, syntaxParam);
    }

    private String hsqldbSyntaxParam(Vendor vendor) {
        switch (vendor) {
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
