package core.framework.internal.web.http;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author neo
 */
public class IPv4Ranges implements IPRanges {
    static boolean withinRanges(int[] ranges, int value) {
        int low = 0;
        int high = ranges.length - 1;
        if (value < ranges[low] || value > ranges[high]) return false;

        while (high - low > 1) {
            int middle = (low + high) >>> 1;
            long middleValue = ranges[middle];

            if (middleValue < value)
                low = middle;
            else if (middleValue > value)
                high = middle;
            else {
                return true;
            }
        }
        return low % 2 == 0;
    }

    static int[] mergeRanges(int[][] ranges) {
        int index;
        Arrays.sort(ranges, Comparator.comparingInt(range -> range[0]));
        int[] results = new int[ranges.length * 2];
        index = 0;
        for (int[] range : ranges) {
            if (index > 1 && results[index - 1] >= range[0]) {
                if (results[index - 1] < range[1]) {
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

    private final int[] ranges;

    public IPv4Ranges(List<String> cidrs) {
        int[][] ranges = new int[cidrs.size()][];
        int index = 0;
        for (String cidr : cidrs) {
            int[] range = sortableRange(cidr);
            ranges[index++] = range;
        }
        this.ranges = mergeRanges(ranges);
    }

    private int[] sortableRange(String cidr) {
        int index = cidr.indexOf('/');
        if (index <= 0 || index >= cidr.length() - 1) throw new Error("invalid cidr, value=" + cidr);
        int maskBits = Integer.parseInt(cidr.substring(index + 1));

        int rangeStart;
        int rangeEnd;
        // refer to https://docs.oracle.com/javase/specs/jls/se22/html/jls-15.html#jls-15.19
        // If the promoted type of the left-hand operand is int, then only the five lowest-order bits of the right-hand operand are used as the shift distance.
        // It is as if the right-hand operand were subjected to a bitwise logical AND operator & (§15.22.1) with the mask value 0x1f (0b11111).
        // The shift distance actually used is therefore always in the range 0 to 31, inclusive.
        if (maskBits == 0) {
            rangeStart = 0x0000_0000;
            rangeEnd = 0xFFFF_FFFF;
        } else {
            int address = toInt(IPRanges.address(cidr.substring(0, index)));
            int mask = 0xFFFF_FFFF << (32 - maskBits);
            rangeStart = address & mask;
            rangeEnd = rangeStart + ~mask;
        }
        return new int[]{toSortable(rangeStart), toSortable(rangeEnd)};
    }

    private int toInt(byte[] address) {
        if (address.length != 4) throw new Error("not ipv4 address, address=" + Arrays.toString(address));
        int result = 0x0000_0000;
        result |= (address[0] & 0xFF) << 24;
        result |= (address[1] & 0xFF) << 16;
        result |= (address[2] & 0xFF) << 8;
        result |= address[3] & 0xFF;
        return result;
    }

    @Override
    public boolean matches(byte[] address) {
        if (ranges.length == 0) return false;
        int sortable = toSortable(toInt(address));
        return withinRanges(ranges, sortable);
    }

    // with address ^ MIN_VALUE, it converts binary presentation to sortable number form (due to java doesn't have unsigned number type)
    // 0.0.0.0 => MIN_VALUE         (00000000.0.0.0 => 10000000.0.0.0)
    // 127.255.255.255 => -1        (01111111.11111111.11111111.11111111 => 11111111.11111111.11111111.11111111)
    // 128.0.0.0 => 0               (10000000.0.0.0 => 00000000.0.0.0)
    // 255.255.255.255 => MAX_VALUE (11111111.11111111.11111111.11111111 => 01111111.11111111.11111111.11111111)
    // refer to Integer.compareUnsigned(x, y)
    private int toSortable(int value) {
        return value ^ Integer.MIN_VALUE;
    }
}
