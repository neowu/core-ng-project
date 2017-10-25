package core.framework.impl.redis.v2;

import core.framework.util.ASCII;
import core.framework.util.Strings;

import java.io.IOException;

/**
 * refer to https://redis.io/topics/protocol
 * here it only supports request-response/pipeline model
 */
public final class Protocol {
    static final int DEFAULT_PORT = 6379;

    private static final byte SIMPLE_STRING_BYTE = '+';
    private static final byte ERROR_BYTE = '-';
    private static final byte INTEGER_BYTE = ':';
    private static final byte BULK_STRING_BYTE = '$';
    private static final byte ARRAY_BYTE = '*';

    static byte[] request(Command command, byte[]... arguments) {
        int argumentLength = Encoder.byteArraySize(arguments.length + 1);
        int commandLength = Encoder.byteArraySize(command.value.length);

        int messageSize = 1 + argumentLength + 2 + 1 + commandLength + 2 + command.value.length + 2;
        for (byte[] argument : arguments) {
            messageSize += 1 + Encoder.byteArraySize(argument.length) + 2 + argument.length + 2;
        }
        int position = 0;
        byte[] message = new byte[messageSize];
        message[position++] = ARRAY_BYTE;
        position += argumentLength;
        Encoder.fill(arguments.length + 1, position, message);
        message[position++] = '\r';
        message[position++] = '\n';
        message[position++] = BULK_STRING_BYTE;
        position += commandLength;
        Encoder.fill(command.value.length, position, message);
        message[position++] = '\r';
        message[position++] = '\n';
        System.arraycopy(command.value, 0, message, position, command.value.length);
        position += command.value.length;
        message[position++] = '\r';
        message[position++] = '\n';
        for (byte[] argument : arguments) {
            message[position++] = BULK_STRING_BYTE;
            position += Encoder.byteArraySize(argument.length);
            Encoder.fill(argument.length, position, message);
            message[position++] = '\r';
            message[position++] = '\n';
            System.arraycopy(argument, 0, message, position, argument.length);
            position += argument.length;
            message[position++] = '\r';
            message[position++] = '\n';
        }

        return message;
    }

    public static Object response(RedisInputStream stream) throws IOException {
        return parseResponse(stream);
    }

    private static Object parseResponse(RedisInputStream stream) throws IOException {
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
                return new Error(message);
            default:
                throw new IOException("unknown redis response, firstByte=" + (char) firstByte);
        }
    }

    static String readErrorIfPossible(RedisInputStream inputStream) throws IOException {
        final byte firstByte = inputStream.readByte();
        if (firstByte != ERROR_BYTE) {  // if buffer contains other type of response, just ignore.
            return null;
        }
        return inputStream.readSimpleString();
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
            array[i] = parseResponse(stream);
        }
        return array;
    }

    public enum Command {
        PING, SET, GET, QUIT, EXISTS, DEL, TYPE, FLUSHDB, KEYS, RANDOMKEY, RENAME, RENAMENX, RENAMEX, DBSIZE, EXPIRE, EXPIREAT, TTL, SELECT, MOVE, FLUSHALL, GETSET, MGET, SETNX, SETEX, MSET, MSETNX, DECRBY, DECR, INCRBY, INCR, APPEND, SUBSTR, HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, RPOPLPUSH, SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD, SISMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE, SDIFF, SDIFFSTORE, SRANDMEMBER, ZADD, ZRANGE, ZREM, ZINCRBY, ZRANK, ZREVRANK, ZREVRANGE, ZCARD, ZSCORE, MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, BLPOP, BRPOP, AUTH, SUBSCRIBE, PUBLISH, UNSUBSCRIBE, PSUBSCRIBE, PUNSUBSCRIBE, PUBSUB, ZCOUNT, ZRANGEBYSCORE, ZREVRANGEBYSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZUNIONSTORE, ZINTERSTORE, ZLEXCOUNT, ZRANGEBYLEX, ZREVRANGEBYLEX, ZREMRANGEBYLEX, SAVE, BGSAVE, BGREWRITEAOF, LASTSAVE, SHUTDOWN, INFO, MONITOR, SLAVEOF, CONFIG, STRLEN, SYNC, LPUSHX, PERSIST, RPUSHX, ECHO, LINSERT, DEBUG, BRPOPLPUSH, SETBIT, GETBIT, BITPOS, SETRANGE, GETRANGE, EVAL, EVALSHA, SCRIPT, SLOWLOG, OBJECT, BITCOUNT, BITOP, SENTINEL, DUMP, RESTORE, PEXPIRE, PEXPIREAT, PTTL, INCRBYFLOAT, PSETEX, CLIENT, TIME, MIGRATE, HINCRBYFLOAT, SCAN, HSCAN, SSCAN, ZSCAN, WAIT, CLUSTER, ASKING, PFADD, PFCOUNT, PFMERGE, READONLY, GEOADD, GEODIST, GEOHASH, GEOPOS, GEORADIUS, GEORADIUSBYMEMBER, BITFIELD;

        final byte[] value;

        Command() {
            value = Strings.bytes(name());
        }
    }

    public enum Keyword {
        AGGREGATE, ALPHA, ASC, BY, DESC, GET, LIMIT, MESSAGE, NO, NOSORT, PMESSAGE, PSUBSCRIBE, PUNSUBSCRIBE, OK, ONE, QUEUED, SET, STORE, SUBSCRIBE, UNSUBSCRIBE, WEIGHTS, WITHSCORES, RESETSTAT, RESET, FLUSH, EXISTS, LOAD, KILL, LEN, REFCOUNT, ENCODING, IDLETIME, AND, OR, XOR, NOT, GETNAME, SETNAME, LIST, MATCH, COUNT, PING, PONG;

        final byte[] value;

        Keyword() {
            value = Strings.bytes(ASCII.toLowerCase(name()));
        }
    }

    public static class Error {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}
