package core.framework.impl.log;

import core.framework.util.Encodings;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
class ActionId {
    private static final AtomicInteger COUNTER = new AtomicInteger(ThreadLocalRandom.current().nextInt());
    private static final int LOW_ORDER_THREE_BYTES = 0xFFFFFF;
    private static final int MACHINE_IDENTIFIER = machineIdentifier() & LOW_ORDER_THREE_BYTES;

    private static int machineIdentifier() {
        try {
            var builder = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                builder.append(networkInterface.getName());
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) builder.append(Encodings.hex(mac));
            }
            builder.append(ThreadLocalRandom.current().nextInt());  // append random value in case multiple apps run on same server (which is highly unlikely with cloud/kube env)
            return builder.toString().hashCode();
        } catch (SocketException e) {
            throw new Error(e);
        }
    }

    // action id doesn't need strict uniqueness as UUID, here to generate shorter and more elasticsearch/lucene friendly id
    static String next() {
        long timeInMs = System.currentTimeMillis();
        int counter = COUNTER.getAndIncrement() & LOW_ORDER_THREE_BYTES;
        byte[] bytes = new byte[10];
        bytes[0] = (byte) (timeInMs >> 32);     // save 5 bytes time in ms, about 34 years value space
        bytes[1] = (byte) (timeInMs >> 24);
        bytes[2] = (byte) (timeInMs >> 16);
        bytes[3] = (byte) (timeInMs >> 8);
        bytes[4] = (byte) timeInMs;
        bytes[5] = (byte) (MACHINE_IDENTIFIER >> 16);   // 3 bytes as machine id, about 16M value space
        bytes[6] = (byte) (MACHINE_IDENTIFIER >> 8);
        bytes[7] = (byte) MACHINE_IDENTIFIER;
        bytes[8] = (byte) (counter >> 8);               // 2 bytes for max 65k actions per ms per server
        bytes[9] = (byte) counter;
        return Encodings.hex(bytes);
    }
}
