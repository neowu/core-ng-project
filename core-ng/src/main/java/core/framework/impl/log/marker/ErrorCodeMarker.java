package core.framework.impl.log.marker;

/**
 * @author neo
 */
public final class ErrorCodeMarker extends AbstractMarker {
    private static final long serialVersionUID = 2633013025655449295L;

    private final String code;

    public ErrorCodeMarker(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return code;
    }
}
