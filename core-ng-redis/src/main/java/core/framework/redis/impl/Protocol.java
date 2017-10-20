package core.framework.redis.impl;

import core.framework.util.ASCII;
import core.framework.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Protocol {
    public static final int DEFAULT_SENTINEL_PORT = 26379;
    static final int DEFAULT_PORT = 6379;

    private static final byte DOLLAR_BYTE = '$';
    private static final byte ASTERISK_BYTE = '*';
    private static final byte PLUS_BYTE = '+';
    private static final byte MINUS_BYTE = '-';
    private static final byte COLON_BYTE = ':';

    static void sendCommand(final RedisOutputStream outputStream, final Command command, final byte[]... arguments) throws IOException {
        outputStream.write(ASTERISK_BYTE);
        outputStream.writeIntCrLf(arguments.length + 1);
        outputStream.write(DOLLAR_BYTE);
        outputStream.writeIntCrLf(command.value.length);
        outputStream.write(command.value);
        outputStream.writeCrLf();

        for (final byte[] arg : arguments) {
            outputStream.write(DOLLAR_BYTE);
            outputStream.writeIntCrLf(arg.length);
            outputStream.write(arg);
            outputStream.writeCrLf();
        }
    }

    public static Object read(final RedisInputStream inputStream) throws IOException {
        return parseResponse(inputStream);
    }

    private static Object parseResponse(RedisInputStream inputStream) throws IOException {
        byte firstByte = inputStream.readByte();
        switch (firstByte) {
            case PLUS_BYTE:
                return inputStream.readLineBytes();
            case DOLLAR_BYTE:
                return processBulkReply(inputStream);
            case ASTERISK_BYTE:
                return processMultiBulkReply(inputStream);
            case COLON_BYTE:
                return inputStream.readLongCRLF();
            case MINUS_BYTE:
                String message = inputStream.readLine();
                throw new RedisException(message);
            default:
                throw new RedisException("unknown response, firstByte=" + (char) firstByte);
        }
    }

    static String readErrorLineIfPossible(RedisInputStream inputStream) throws IOException {
        final byte firstByte = inputStream.readByte();
        if (firstByte != MINUS_BYTE) {  // if buffer contains other type of response, just ignore.
            return null;
        }
        return inputStream.readLine();
    }

    private static byte[] processBulkReply(final RedisInputStream inputStream) throws IOException {
        final int len = (int) inputStream.readLongCRLF();
        if (len == -1) {
            return null;
        }

        final byte[] read = new byte[len];
        int offset = 0;
        while (offset < len) {
            final int size = inputStream.read(read, offset, (len - offset));
            if (size == -1) throw new RedisException("It seems like server has closed the connection.");
            offset += size;
        }

        // read 2 more bytes for the command delimiter
        inputStream.readByte();
        inputStream.readByte();

        return read;
    }

    private static List<Object> processMultiBulkReply(final RedisInputStream inputStream) throws IOException {
        final int num = (int) inputStream.readLongCRLF();
        if (num == -1) {
            return null;
        }
        final List<Object> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            try {
                ret.add(parseResponse(inputStream));
            } catch (RedisException e) {
                ret.add(e);
            }
        }
        return ret;
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
}
