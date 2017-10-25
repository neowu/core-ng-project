package core.framework.impl.redis.v2;

/**
 * refer to jedis and redisson impl,
 * https://github.com/redisson/redisson/blob/master/redisson/src/main/java/org/redisson/client/handler/CommandEncoder.java
 * https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients/util/RedisOutputStream.java
 *
 * @author neo
 */
public class Encoder {
    private static final char[] DIGIT_TENS = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};
    private static final char[] DIGIT_ONES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    private static final byte[][] CACHE = new byte[256][];

    static {
        for (int i = 0; i < 256; i++) {
            CACHE[i] = toBytes(i);
        }
    }

    public static byte[] bytes(int number) {
        if (number >= 0 && number <= 255) {
            return CACHE[number];
        }
        return toBytes(number);
    }

    private static byte[] toBytes(int number) {
        int size = (number < 0) ? byteArraySize(-number) + 1 : byteArraySize(number);
        byte[] buf = new byte[size];
        fill(number, size, buf);
        return buf;
    }

    static int byteArraySize(int x) {
        int size = 0;
        while (true) {
            if (x <= SIZE_TABLE[size]) {
                return size + 1;
            }
            size++;
        }
    }

    static void fill(int number, int index, byte[] buffer) {
        int q, r;
        int position = index;
        byte sign = 0;

        if (number < 0) {
            sign = '-';
            number = -number;
        }

        // generate two digits per iteration
        while (number >= 65536) {
            q = number / 100;
            // r = number - (q * 100);
            r = number - ((q << 6) + (q << 5) + (q << 2));
            number = q;
            buffer[--position] = (byte) DIGIT_ONES[r];
            buffer[--position] = (byte) DIGIT_TENS[r];
        }

        // fall thru to fast mode for smaller numbers
        while (true) {
            q = (number * 52429) >>> (16 + 3);
            r = number - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buffer[--position] = (byte) DIGITS[r];
            number = q;
            if (number == 0)
                break;
        }
        if (sign != 0) {
            buffer[--position] = sign;
        }
    }

    public static void main(String[] args) {
        System.out.println(new String(Encoder.bytes(1999999999)));
        System.out.println(new String(Encoder.bytes(-1999999999)));
        System.out.println(new String(Encoder.bytes(10)));
    }
}
