package core.framework.impl.log;

/**
 * @author neo
 */
final class PerformanceStat {
    int count;
    long elapsedTime;
    Integer readEntries;
    Integer writeEntries;

    void track(long elapsedTime, Integer readEntries, Integer writeEntries) {
        count++;
        this.elapsedTime += elapsedTime;
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
