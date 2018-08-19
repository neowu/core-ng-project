package core.framework.impl.redis;

import core.framework.util.Strings;

import java.io.IOException;

/**
 * refer to https://redis.io/topics/protocol, this only supports request-response/pipeline model
 */
final class Protocol {
    private static final byte SIMPLE_STRING_BYTE = '+';
    private static final byte ERROR_BYTE = '-';
    private static final byte INTEGER_BYTE = ':';
    private static final byte BULK_STRING_BYTE = '$';
    private static final byte ARRAY_BYTE = '*';

    static void writeArray(RedisOutputStream stream, int length) throws IOException {
        stream.write(ARRAY_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(length));
    }

    static void writeBulkString(RedisOutputStream stream, byte[] value) throws IOException {
        stream.write(BULK_STRING_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(value.length));
        stream.writeBytesCRLF(value);
    }

    static Object read(RedisInputStream stream) throws IOException {
        return parseObject(stream);
    }

    private static Object parseObject(RedisInputStream stream) throws IOException {
        byte firstByte = stream.readByte();
        switch (firstByte) {
            case SIMPLE_STRING_BYTE:
                return stream.readSimpleString();
            case BULK_STRING_BYTE:
                return parseBulkString(stream);
            case ARRAY_BYTE:
                return parseArray(stream);
            case INTEGER_BYTE:
                return stream.readLong();
            case ERROR_BYTE:
                String message = stream.readSimpleString();
                throw new RedisException(message);
            default:
                throw new IOException("unknown redis response, firstByte=" + (char) firstByte);
        }
    }

    private static byte[] parseBulkString(RedisInputStream stream) throws IOException {
        int length = (int) stream.readLong();
        if (length == -1) return null;

        return stream.readBulkString(length);
    }

    private static Object[] parseArray(RedisInputStream stream) throws IOException {
        int length = (int) stream.readLong();
        if (length == -1) return null;

        var array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = parseObject(stream);       // redis won't put error within array, so here it doesn't expect RedisException
        }
        return array;
    }

    static class Command {
        static final byte[] GET = Strings.bytes("GET");
        static final byte[] SET = Strings.bytes("SET");
        static final byte[] EXPIRE = Strings.bytes("EXPIRE");
        static final byte[] DEL = Strings.bytes("DEL");
        static final byte[] INCRBY = Strings.bytes("INCRBY");
        static final byte[] MGET = Strings.bytes("MGET");
        static final byte[] MSET = Strings.bytes("MSET");
        static final byte[] SCAN = Strings.bytes("SCAN");
        static final byte[] HGET = Strings.bytes("HGET");
        static final byte[] HGETALL = Strings.bytes("HGETALL");
        static final byte[] HSET = Strings.bytes("HSET");
        static final byte[] HMSET = Strings.bytes("HMSET");
        static final byte[] HDEL = Strings.bytes("HDEL");
        static final byte[] SADD = Strings.bytes("SADD");
        static final byte[] SMEMBERS = Strings.bytes("SMEMBERS");
        static final byte[] SISMEMBER = Strings.bytes("SISMEMBER");
        static final byte[] SREM = Strings.bytes("SREM");
        static final byte[] RPUSH = Strings.bytes("RPUSH");
        static final byte[] LPOP = Strings.bytes("LPOP");
        static final byte[] LRANGE = Strings.bytes("LRANGE");
    }

    static class Keyword {
        static final byte[] MATCH = Strings.bytes("match");
        static final byte[] COUNT = Strings.bytes("count");
        static final byte[] NX = Strings.bytes("nx");
        static final byte[] EX = Strings.bytes("ex");
    }
}
