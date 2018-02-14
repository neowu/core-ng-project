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

    static void write(RedisOutputStream stream, Command command, byte[]... arguments) throws IOException {
        stream.write(ARRAY_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(arguments.length + 1));
        stream.write(BULK_STRING_BYTE);
        stream.writeBytesCRLF(RedisEncodings.encode(command.value.length));
        stream.writeBytesCRLF(command.value);
        for (byte[] argument : arguments) {
            stream.write(BULK_STRING_BYTE);
            stream.writeBytesCRLF(RedisEncodings.encode(argument.length));
            stream.writeBytesCRLF(argument);
        }
        stream.flush();
    }

    static Object read(RedisInputStream stream) throws IOException {
        return parse(stream);
    }

    private static Object parse(RedisInputStream stream) throws IOException {
        byte firstByte = stream.readByte();
        switch (firstByte) {
            case SIMPLE_STRING_BYTE:
                return stream.readSimpleString();
            case BULK_STRING_BYTE:
                return parseBulkString(stream);
            case ARRAY_BYTE:
                return parseArray(stream);
            case INTEGER_BYTE:
                return stream.readLongCRLF();
            case ERROR_BYTE:
                String message = stream.readSimpleString();
                throw new RedisException(message);
            default:
                throw new IOException("unknown redis response, firstByte=" + (char) firstByte);
        }
    }

    private static byte[] parseBulkString(RedisInputStream stream) throws IOException {
        int length = (int) stream.readLongCRLF();
        if (length == -1) {
            return null;
        }
        return stream.readBulkStringCRLF(length);
    }

    private static Object[] parseArray(RedisInputStream stream) throws IOException {
        int length = (int) stream.readLongCRLF();
        if (length == -1) {
            return null;
        }
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = parse(stream);       // redis won't put error within array, so here it doesn't expect RedisException
        }
        return array;
    }

    enum Command {
        GET, SET, SETEX, EXPIRE, DEL, INCRBY, MGET, MSET, SCAN, HGET, HGETALL, HSET, HMSET, HDEL, SADD, SMEMBERS, SISMEMBER, SREM;
        final byte[] value;

        Command() {
            value = Strings.bytes(name());
        }
    }

    static class Keyword {
        static final byte[] MATCH = Strings.bytes("match");
        static final byte[] COUNT = Strings.bytes("count");
        static final byte[] NX = Strings.bytes("nx");
        static final byte[] EX = Strings.bytes("ex");
    }
}
