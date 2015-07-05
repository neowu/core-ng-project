package core.framework.impl.db;

/**
 * @author neo
 */
public enum TestEnum {
    V1("v1"), V2("v2");

    private final String code;

    TestEnum(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
