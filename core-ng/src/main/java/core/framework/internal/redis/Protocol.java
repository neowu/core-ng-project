package core.framework.internal.redis;

import core.framework.util.Strings;

import java.io.IOException;

/**
 * refer to https://github.com/antirez/RESP3/blob/master/spec.md, currently only support RESP2
 */
final class Protocol {
    private static final byte BLOB_STRING_BYTE = '$';
    private static final byte SIMPLE_STRING_BYTE = '+';
    private static final byte SIMPLE_ERROR_BYTE = '-';
    private static final byte NUMBER_BYTE = ':';
    private static final byte ARRAY_BYTE = '*';

    static void writeArray(RedisOutputStream stream, int length) throws IOException {
        stream.write(ARRAY_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(length));
    }

    static void writeBlobString(RedisOutputStream stream, byte[] value) throws IOException {
        stream.write(BLOB_STRING_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(value.length));
        stream.writeBytesCRLF(value);
    }

    static Object read(RedisInputStream stream) throws IOException {
        return parseObject(stream);
    }

    private static Object parseObject(RedisInputStream stream) throws IOException {
        byte firstByte = stream.readByte();
        return switch (firstByte) {
            case SIMPLE_STRING_BYTE -> stream.readSimpleString();
            case BLOB_STRING_BYTE -> parseBlobString(stream);
            case ARRAY_BYTE -> parseArray(stream);
            case NUMBER_BYTE -> stream.readLong();
            case SIMPLE_ERROR_BYTE -> {
                String message = stream.readSimpleString();
                throw new RedisException(message);
            }
            default -> throw new IOException("unknown redis response, firstByte=" + (char) firstByte);
        };
    }

    private static byte[] parseBlobString(RedisInputStream stream) throws IOException {
        int length = (int) stream.readLong();
        if (length == -1) return null;

        return stream.readBytes(length);
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
        static final byte[] AUTH = Strings.bytes("AUTH");
        static final byte[] INFO = Strings.bytes("INFO");

        static final byte[] GET = Strings.bytes("GET");
        static final byte[] SET = Strings.bytes("SET");
        static final byte[] PEXPIRE = Strings.bytes("PEXPIRE");
        static final byte[] PTTL = Strings.bytes("PTTL");
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
        static final byte[] HINCRBY = Strings.bytes("HINCRBY");

        static final byte[] PFADD = Strings.bytes("PFADD");
        static final byte[] PFCOUNT = Strings.bytes("PFCOUNT");

        static final byte[] SADD = Strings.bytes("SADD");
        static final byte[] SMEMBERS = Strings.bytes("SMEMBERS");
        static final byte[] SISMEMBER = Strings.bytes("SISMEMBER");
        static final byte[] SREM = Strings.bytes("SREM");
        static final byte[] SPOP = Strings.bytes("SPOP");
        static final byte[] SCARD = Strings.bytes("SCARD");

        static final byte[] LRANGE = Strings.bytes("LRANGE");
        static final byte[] RPUSH = Strings.bytes("RPUSH");
        static final byte[] LPOP = Strings.bytes("LPOP");
        static final byte[] LTRIM = Strings.bytes("LTRIM");

        static final byte[] ZADD = Strings.bytes("ZADD");
        static final byte[] ZINCRBY = Strings.bytes("ZINCRBY");
        static final byte[] ZRANGE = Strings.bytes("ZRANGE");
        static final byte[] ZREM = Strings.bytes("ZREM");
        static final byte[] ZPOPMIN = Strings.bytes("ZPOPMIN");
    }

    static class Keyword {
        static final byte[] MATCH = Strings.bytes("MATCH");
        static final byte[] COUNT = Strings.bytes("COUNT");
        static final byte[] NX = Strings.bytes("NX");
        static final byte[] PX = Strings.bytes("PX");
        static final byte[] LIMIT = Strings.bytes("LIMIT");
        static final byte[] WITHSCORES = Strings.bytes("WITHSCORES");
        static final byte[] BYSCORE = Strings.bytes("BYSCORE");
    }
}
