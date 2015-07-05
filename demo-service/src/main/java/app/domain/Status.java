package app.domain;

/**
 * @author neo
 */
public enum Status {
    ACTIVE("A"), INACTIVE("I");

    public final String code;

    Status(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
