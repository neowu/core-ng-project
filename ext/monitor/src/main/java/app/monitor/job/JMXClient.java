package app.monitor.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * @author neo
 */
public class JMXClient {
    static {
        try {
            RMISocketFactory.setSocketFactory(new RMISocketFactory() {
                @Override
                public Socket createSocket(String host, int port) throws IOException {
                    var socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), 5000);    // set connect and read timeout to 5s
                    socket.setSoTimeout(5000);
                    return socket;
                }

                @Override
                public ServerSocket createServerSocket(int port) throws IOException {
                    return new ServerSocket(port);
                }
            });
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    static ObjectName objectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new Error(e);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(JMXClient.class);
    private final JMXServiceURL serviceURL;
    private JMXConnector jmx;

    public JMXClient(String host) {
        try {
            serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":1099/jmxrmi");
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public MBeanServerConnection connect() throws IOException {
        if (!check(jmx)) {
            jmx = JMXConnectorFactory.connect(serviceURL);
        }
        return jmx.getMBeanServerConnection();
    }

    boolean check(JMXConnector jmx) {
        if (jmx == null) return false;
        try {
            jmx.getConnectionId();
            return true;
        } catch (IOException e) {
            close(jmx);
            return false;
        }
    }

    private void close(JMXConnector jmx) {
        try {
            jmx.close();
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
