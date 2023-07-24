package network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetworkConfig {
    public static int SERVER_PORT;
    public static String SERVER_IP;

    static {
        setServerIp();
    }

    // This method is used to set the SERVER_IP variable
    // remember to handle the UnknownHostException
    private static void setServerIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    SERVER_IP = addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Failed to determine the host address", e);
        }
    }
}
