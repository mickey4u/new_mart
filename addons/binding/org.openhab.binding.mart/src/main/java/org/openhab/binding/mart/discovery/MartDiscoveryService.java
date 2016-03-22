package org.openhab.binding.mart.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.mart.handler.martHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MartDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(MartDiscoveryService.class);

    /**
     * Connection timeout
     */
    private static int TIMEOUT = 5000;
    /**
     * Port to send discovery message to
     */
    private static final int SSDP_PORT = 1900;
    /**
     * Port to use for sending discovery message
     */
    private static final int SSDP_SEARCH_PORT = 1901;
    /**
     * Broadcast address to use for sending discovery message
     */
    private static final String SSDP_IP = "239.255.255.250";

    public InetAddress address;

    public MartDiscoveryService() {
        super(martHandler.SUPPORTTED_THING_TYPES, 15, true);

    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return super.getSupportedThingTypes();
    }

    @Override
    protected void startScan() {

    }

    @Override
    protected void startBackgroundDiscovery() {

        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {

        super.stopBackgroundDiscovery();
    }

    public void sendMartDiscoveryMessage() {
        logger.debug("MART discovery is starting");

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            InetSocketAddress srcAddress = new InetSocketAddress(localhost, SSDP_SEARCH_PORT);
            InetSocketAddress dstAddress = new InetSocketAddress(InetAddress.getByName(SSDP_IP), SSDP_PORT);

            DatagramPacket discoveryPacket = null;

            // send multicast packet

            // Response Listener
            DatagramSocket martReceiveSocket = null;
            DatagramPacket receivePacket = null;

            try {
                // Constructs a datagram socket and binds it to any available port on the local host machine
                martReceiveSocket = new DatagramSocket(SSDP_SEARCH_PORT);
                martReceiveSocket.setSoTimeout(TIMEOUT);
                logger.debug("Send datagram packet.");
                martReceiveSocket.send(discoveryPacket);

                while (true) {
                    try {
                        receivePacket = new DatagramPacket(new byte[1536], 1536);
                        martReceiveSocket.receive(receivePacket);
                        final String message = new String(receivePacket.getData());
                        logger.trace("Received message: {}", message);

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                            }
                        }).start();
                    } catch (SocketTimeoutException e) {
                        // TODO: handle exception
                    }

                }

            } finally {
                if (martReceiveSocket != null) {
                    martReceiveSocket.disconnect();
                    martReceiveSocket.close();
                }

            }

        } catch (Exception e) {
            logger.error("Could not sent mart device discovery ", e);
        }
    }

}
