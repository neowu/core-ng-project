package core.framework.impl.log;

/**
 * @author neo
 */
final class PerformanceStat {
    long totalElapsed;
    int count;
    Integer readEntries;
    Integer writeEntries;

    public void increaseReadEntries(Integer entries) {
        if (entries != null) {
            if (readEntries == null) readEntries = entries;
            else readEntries += entries;
        }
    }

    public void increaseWriteEntries(Integer entries) {
        if (entries != null) {
            if (writeEntries == null) writeEntries = entries;
            else writeEntries += entries;
        }
    }
}
