package core.framework.impl.log.marker;

import org.slf4j.Marker;

import java.util.Iterator;

/**
 * @author neo
 */
public abstract class AbstractMarker implements Marker {
    private static final long serialVersionUID = 8406894019332263130L;

    @Override
    public void add(Marker reference) {
    }

    @Override
    public boolean remove(Marker reference) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<Marker> iterator() {
        return null;
    }

    @Override
    public boolean contains(Marker other) {
        return false;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
