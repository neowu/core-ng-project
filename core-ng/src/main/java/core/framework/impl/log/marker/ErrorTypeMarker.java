package core.framework.impl.log.marker;

/**
 * @author neo
 */
public final class ErrorTypeMarker extends AbstractMarker {
    private static final long serialVersionUID = 2633013025655449295L;

    private final String type;

    public ErrorTypeMarker(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return type;
    }
}
