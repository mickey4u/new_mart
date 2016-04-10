/*
 * package org.openhab.binding.mart.discovery;
 * 
 * import java.net.DatagramPacket;
 * import java.net.DatagramSocket;
 * import java.net.InetAddress;
 * import java.net.InetSocketAddress;
 * import java.net.MulticastSocket;
 * import java.net.SocketTimeoutException;
 * import java.util.Set;
 * import java.util.concurrent.ScheduledFuture;
 * import java.util.concurrent.TimeUnit;
 * 
 * import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
 * import org.eclipse.smarthome.core.thing.ThingTypeUID;
 * import org.openhab.binding.mart.handler.MartHandler;
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 * 
 * public class MartDiscoveryService extends AbstractDiscoveryService {
 * 
 * private Logger logger = LoggerFactory.getLogger(MartDiscoveryService.class);
 * 
 *//**
   * Connection timeout
   */
/*
 * 
 * private static int TIMEOUT = 5000;
 *//**
   * Port to send discovery message to
   */
/*
 * 
 * private static final int SSDP_PORT = 1900;
 *//**
   * Port to use for sending discovery message
   */
/*
 * 
 * private static final int SSDP_SEARCH_PORT = 7091;
 *//**
   * Broadcast address to use for sending discovery message
   */
/*
 * private static final String SSDP_IP = "239.255.255.250";
 * 
 * public InetAddress address;
 * 
 *//**
   * When true we will keep sending out packets
   */
/*
 * 
 * static boolean discoveryRunning = false;
 * 
 *//**
   * The refresh interval for discovering WeMo devices
   */
/*
 * private long refreshInterval = 600;
 * private ScheduledFuture<?> martDiscoveryJob;
 * private Runnable martDiscoveryRunnable = new Runnable() {
 * 
 * @Override
 * public void run() {
 * discoverMART();
 * }
 * };
 * 
 * public MartDiscoveryService() {
 * super(MartHandler.SUPPORTTED_THING_TYPES, 15, true);
 * 
 * }
 * 
 * @Override
 * public Set<ThingTypeUID> getSupportedThingTypes() {
 * return MartHandler.SUPPORTTED_THING_TYPES;
 * }
 * 
 * @Override
 * protected void startScan() {
 * logger.debug("Starting MART adapter discovery");
 * discoverMART();
 * 
 * }
 * 
 * @Override
 * protected void startBackgroundDiscovery() {
 * 
 * logger.trace("Start MART adapter background discovery");
 * if (martDiscoveryJob == null || martDiscoveryJob.isCancelled()) {
 * martDiscoveryJob = scheduler.scheduleAtFixedRate(martDiscoveryRunnable, 0, refreshInterval,
 * TimeUnit.SECONDS);
 * }
 * 
 * }
 * 
 * @Override
 * protected void stopBackgroundDiscovery() {
 * 
 * logger.debug("Stop WeMo device background discovery");
 * if (martDiscoveryJob != null && !martDiscoveryJob.isCancelled()) {
 * martDiscoveryJob.cancel(true);
 * martDiscoveryJob = null;
 * }
 * }
 * 
 *//**
   * Scans for MART devices
   *//*
     * private synchronized void discoverMART() {
     * logger.debug("Run MART adapter discovery");
     * sendMartDiscoveryMessage();
     * logger.trace("Done sending MART broadcast discovery message");
     * // receiveWemoDiscoveryMessage();
     * logger.trace("Done receiving MART broadcast discovery message");
     * }
     * 
     * public void sendMartDiscoveryMessage() {
     * logger.debug("MART discovery is starting");
     * 
     * try {
     * InetAddress localhost = InetAddress.getLocalHost();
     * InetSocketAddress srcAddress = new InetSocketAddress(localhost, SSDP_SEARCH_PORT);
     * InetSocketAddress dstAddress = new InetSocketAddress(InetAddress.getByName(SSDP_IP), SSDP_PORT);
     * 
     * // Request-Packet-Constructor
     * StringBuffer discoveryMessage = new StringBuffer();
     * discoveryMessage.append("Michael");
     * logger.trace("Request: {}", discoveryMessage.toString());
     * byte[] discoveryMessageBytes = discoveryMessage.toString().getBytes();
     * DatagramPacket discoveryPacket = new DatagramPacket(discoveryMessageBytes, discoveryMessageBytes.length,
     * dstAddress);
     * 
     * // send multicast packet
     * MulticastSocket multicast = null;
     * try {
     * multicast = new MulticastSocket(null);
     * multicast.bind(srcAddress);
     * logger.trace("Source-Address = '{}'", srcAddress);
     * multicast.setTimeToLive(4);
     * logger.debug("Send multicast request.");
     * multicast.send(discoveryPacket);
     * } finally {
     * logger.trace("Multicast ends. Close connection.");
     * if (multicast != null) {
     * multicast.disconnect();
     * multicast.close();
     * }
     * }
     * 
     * // Response Listener
     * DatagramSocket martReceiveSocket = null;
     * DatagramPacket receivePacket = null;
     * 
     * try {
     * // Constructs a datagram socket and binds it to any available port on the local host machine
     * martReceiveSocket = new DatagramSocket(SSDP_SEARCH_PORT);
     * martReceiveSocket.setSoTimeout(TIMEOUT);
     * logger.debug("Send datagram packet.");
     * martReceiveSocket.send(discoveryPacket);
     * 
     * while (true) {
     * try {
     * receivePacket = new DatagramPacket(new byte[1536], 1536);
     * martReceiveSocket.receive(receivePacket);
     * final String message = new String(receivePacket.getData());
     * logger.trace("Received message: {}", message);
     * 
     * new Thread(new Runnable() {
     * 
     * @Override
     * public void run() {
     * // TODO Auto-generated method stub
     * 
     * }
     * }).start();
     * } catch (SocketTimeoutException e) {
     * logger.debug("Message receive timed out.");
     * break;
     * }
     * 
     * }
     * 
     * } finally {
     * if (martReceiveSocket != null) {
     * martReceiveSocket.disconnect();
     * martReceiveSocket.close();
     * }
     * 
     * }
     * 
     * } catch (Exception e) {
     * logger.error("Could not sent mart device discovery ", e);
     * }
     * }
     * 
     * }
     */