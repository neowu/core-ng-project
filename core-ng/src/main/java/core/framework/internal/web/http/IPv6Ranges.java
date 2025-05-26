package core.framework.internal.web.http;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class IPv6Ranges implements IPRanges {
    static boolean withinRanges(LongLong[] ranges, LongLong value) {
        int low = 0;
        int high = ranges.length - 1;

        if (value.compareTo(ranges[low]) < 0 || value.compareTo(ranges[high]) > 0) return false;

        while (high - low > 1) {
            int middle = (low + high) >>> 1;
            LongLong middleValue = ranges[middle];

            int comparison = middleValue.compareTo(value);
            if (comparison < 0)
                low = middle;
            else if (comparison > 0)
                high = middle;
            else {
                return true;
            }
        }
        return low % 2 == 0;
    }

    static LongLong[] mergeRanges(LongLong[][] ranges) {
        Arrays.sort(ranges, Comparator.comparing(a -> a[0]));
        LongLong[] results = new LongLong[ranges.length * 2];
        int index = 0;
        for (LongLong[] range : ranges) {
            if (index > 1 && results[index - 1].compareTo(range[0]) >= 0) {
                if (results[index - 1].compareTo(range[1]) < 0) {
                    results[index - 1] = range[1];
                }
            } else {
                results[index++] = range[0];
                results[index++] = range[1];
            }
        }
        if (index < results.length) {
            return Arrays.copyOf(results, index);
        } else {
            return results;
        }
    }

    static LongLong toLongLong(byte[] address) {
        if (address.length != 16) throw new Error("not ipv6 address, address=" + Arrays.toString(address));

        long high = 0;
        high |= (long) (address[0] & 0xFF) << 56;
        high |= (long) (address[1] & 0xFF) << 48;
        high |= (long) (address[2] & 0xFF) << 40;
        high |= (long) (address[3] & 0xFF) << 32;
        high |= (long) (address[4] & 0xFF) << 24;
        high |= (long) (address[5] & 0xFF) << 16;
        high |= (long) (address[6] & 0xFF) << 8;
        high |= address[7] & 0xFF;

        long low = 0;
        low |= (long) (address[8] & 0xFF) << 56;
        low |= (long) (address[9] & 0xFF) << 48;
        low |= (long) (address[10] & 0xFF) << 40;
        low |= (long) (address[11] & 0xFF) << 32;
        low |= (long) (address[12] & 0xFF) << 24;
        low |= (long) (address[13] & 0xFF) << 16;
        low |= (long) (address[14] & 0xFF) << 8;
        low |= address[15] & 0xFF;

        return new LongLong(high, low);
    }

    private final LongLong[] ranges;

    public IPv6Ranges(List<String> cidrs) {
        LongLong[][] ranges = new LongLong[cidrs.size()][];
        int index = 0;
        for (String cidr : cidrs) {
            LongLong[] range = sortableRange(cidr);
            ranges[index++] = range;
        }
        this.ranges = mergeRanges(ranges);
    }

    private LongLong[] sortableRange(String cidr) {
        int index = cidr.indexOf('/');
        if (index <= 0 || index >= cidr.length() - 1) throw new Error("invalid cidr, value=" + cidr);
        LongLong address = toLongLong(IPRanges.address(cidr.substring(0, index)));
        int maskBits = Integer.parseInt(cidr.substring(index + 1));

        LongLong rangeStart;
        LongLong rangeEnd;
        if (maskBits == 0) {
            rangeStart = new LongLong(0L, 0L);
            rangeEnd = new LongLong(-1L, -1L);
        } else if (maskBits <= 64) {
            long highMask = -1L << (64 - maskBits);
            rangeStart = new LongLong(address.high & highMask, 0L);
            rangeEnd = new LongLong(rangeStart.high | ~highMask, -1L);
        } else {
            long lowMask = -1L << (128 - maskBits);
            rangeStart = new LongLong(address.high, address.low & lowMask);
            rangeEnd = new LongLong(address.high, rangeStart.low | ~lowMask);
        }

        return new LongLong[]{rangeStart.toSortable(), rangeEnd.toSortable()};
    }

    @Override
    public boolean matches(byte[] address) {
        if (ranges.length == 0) return false;
        if (address.length != 16) return false;

        LongLong sortable = toLongLong(address).toSortable();
        return withinRanges(ranges, sortable);
    }

    // high,low are used to represent a 128-bit IPv6 address as two 64-bit long values
    record LongLong(long high, long low) implements Comparable<LongLong> {
        // with address ^ MIN_VALUE, it converts binary presentation to sortable number form (due to java doesn't have unsigned number type)
        // 0.0.0.0 => MIN_VALUE         (00000000.0.0.0 => 10000000.0.0.0)
        // 127.255.255.255 => -1        (01111111.11111111.11111111.11111111 => 11111111.11111111.11111111.11111111)
        // 128.0.0.0 => 0               (10000000.0.0.0 => 00000000.0.0.0)
        // 255.255.255.255 => MAX_VALUE (11111111.11111111.11111111.11111111 => 01111111.11111111.11111111.11111111)
        LongLong toSortable() {
            return new LongLong(high ^ Long.MIN_VALUE, low ^ Long.MIN_VALUE);
        }

        @Override
        public int compareTo(LongLong other) {
            int result = Long.compare(high, other.high);
            return result != 0 ? result : Long.compare(low, other.low);
        }
    }
}
