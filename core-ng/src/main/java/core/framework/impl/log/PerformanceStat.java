package core.framework.impl.log;

/**
 * @author neo
 */
final class PerformanceStat {
    int count;
    long totalElapsed;
    Integer readEntries;
    Integer writeEntries;

    void track(long elapsed, Integer readEntries, Integer writeEntries) {
        count++;
        this.totalElapsed += elapsed;
        if (readEntries != null) {
            if (this.readEntries == null) this.readEntries = readEntries;
            else this.readEntries += readEntries;
        }
        if (writeEntries != null) {
            if (this.writeEntries == null) this.writeEntries = writeEntries;
            else this.writeEntries += writeEntries;
        }
    }
}
