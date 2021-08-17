package core.framework.internal.web.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author neo
 */
public class IPv4Ranges {
    static byte[] address(String address) {
        try {
            return InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

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
            int[] range = comparableIPRanges(cidr);
            ranges[index++] = range;
        }
        this.ranges = mergeRanges(ranges);
    }

    private int[] comparableIPRanges(String cidr) {
        int index = cidr.indexOf('/');
        if (index <= 0 || index >= cidr.length() - 1) throw new Error("invalid cidr, value=" + cidr);
        int address = toInteger(address(cidr.substring(0, index)));
        int maskBits = Integer.parseInt(cidr.substring(index + 1));
        long mask = -1L << (32 - maskBits);
        int lowestIP = (int) (address & mask);
        int highestIP = (int) (lowestIP + ~mask);
        return new int[]{lowestIP ^ Integer.MIN_VALUE, highestIP ^ Integer.MIN_VALUE};
    }

    private int toInteger(byte[] address) {
        if (address.length > 4) throw new Error("only support ipv4, address=" + Arrays.toString(address));
        int result = 0;
        result |= (address[0] & 0xFF) << 24;
        result |= (address[1] & 0xFF) << 16;
        result |= (address[2] & 0xFF) << 8;
        result |= address[3] & 0xFF;
        return result;
    }

    /*
     * with address ^ Integer.MIN_VALUE, it converts binary presentation to sortable int form
     * where Integer.MIN_VALUE = 0.0.0.0
     * and 0 = 128.0.0.0
     * and Integer.MAX_VALUE = 255.255.255.255
     * */
    public boolean matches(byte[] address) {
        if (ranges.length == 0) return false;
        int comparableIP = toInteger(address) ^ Integer.MIN_VALUE;
        return withinRanges(ranges, comparableIP);
    }
}
