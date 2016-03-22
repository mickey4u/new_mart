/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mart.handler;

import static org.openhab.binding.mart.martBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link martHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kwaku Tetteh - Initial contribution
 */
public class martHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(martHandler.class);

    public final static Set<ThingTypeUID> SUPPORTTED_THING_TYPES = Sets.newHashSet(THING_TYPE_MART_ADAPTER);

    /**
     * A Selector is a Java NIO component which can examine one or more NIO Channel's, and determine which channels are
     * ready for e.g. reading or writing. This way a single thread can manage multiple channels, and thus multiple
     * network connections. A selector is an object that can monitor multiple channels for events .
     * The advantage of using just a single thread to handle multiple channels is that you need less threads to handle
     * the channels
     */
    private Selector selector;
    /**
     * A Java NIO DatagramChannel is a channel that can send and receive UDP packets. Since UDP is a connection-less
     * network protocol, you cannot just by default read and write to a DatagramChannel like you do from other channels.
     * Instead you send and receive packets of data.
     */
    private DatagramChannel datagramChannel = null;
    protected DatagramChannel listenerChannel = null;
    /**
     * A token representing the registration of a SelectableChannel with a Selector.
     * A selection key is created each time a channel is registered with a selector.
     */
    protected SelectionKey datagramChannelKey = null;
    protected SelectionKey listenerKey = null;
    /**
     *
     */
    private final Lock lock = new ReentrantLock();
    protected JsonParser parser = new JsonParser();
    /**
     * these are to schedule a task to execute repeatedly with a fixed interval of time
     * in between each execution
     */
    private ScheduledFuture<?> listeningJob;
    private ScheduledFuture<?> pollingJob;

    private static final String IP_ADDRESS = "ipAddress";
    private static final String POLLING_REFRESH_INTERVAL = "refreshInterval";
    public static final int INITIAL_DELAY = 0;
    public static final int CONNECTION_REFRESH_INTERVAL = 100;

    public static final int LISTENER_PORT_NUMBER = 7090;
    public static final int REMOTE_PORT_NUMBER = 7090;
    public static final int PING_TIME_OUT = 3000;
    public static final int BUFFER_SIZE = 1024;

    public static final String ADDRESS = null;

    public martHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_STATE:
                // check if the command is an ON/OFF command
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        // send on command
                        sendMartCommand("On Adapter");
                    } else if (command == OnOffType.OFF) {
                        // send off command
                        sendMartCommand("Off Adapter");
                    } else {
                        return;
                    }
                }
                break;
            case CHANNEL_FRIDGE:
                // check if the command is an ON/OFF command
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        // send on command
                        sendMartCommand("On Fridge");
                    } else if (command == OnOffType.OFF) {
                        // send off command
                        sendMartCommand("Off Fridge");
                    } else {
                        return;
                    }
                }
                break;
            case CHANNEL_OUTSIDE_LIGHT:
                // check if the command is an ON/OFF command
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        // send on command
                        sendMartCommand("On Outside Light");
                    } else if (command == OnOffType.OFF) {
                        // send off command
                        sendMartCommand("Off Outside Light");
                    } else {
                        return;
                    }
                }
                break;
            case CHANNEL_TELEVISION:
                // check if the command is an ON/OFF command
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        // send on command
                        sendMartCommand("On Television");
                    } else if (command == OnOffType.OFF) {
                        // send off command
                        sendMartCommand("Off Television");
                    } else {
                        return;
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        logger.debug("Initializing Mart Smart Adapter handler '{}'", getThing().getUID());
        // Long running initialization should be done asynchronously in background.

        try {
            // open the selector
            selector = Selector.open();
        } catch (IOException e) {
            // TODO: handle exception
            logger.error("An IOException occurred while registering the selector: '{}'", e.getMessage());
        }

        // create a listener channel given a port number
        createListenerChannel(LISTENER_PORT_NUMBER);

        // returns the configuration of the thing .get the ip address of the thing
        if (getConfig().get(IP_ADDRESS) != null && getConfig().get(IP_ADDRESS) != "") {

            // establish a connection
            establishConnection();

            if (listeningJob == null || listeningJob.isCancelled()) {

                try {
                    // Creates and executes a periodic action that becomes enabled first after the given initial delay,
                    // and subsequently with the given delay between the termination of one execution and the
                    // commencement
                    // of the next
                    // In this method, however, the period is interpreted as the delay between the end of the previous
                    // execution,
                    // until the start of the next. The delay is thus between finished executions, not between the
                    // beginning of
                    // executions
                    listeningJob = scheduler.scheduleWithFixedDelay(listeningRunnable, INITIAL_DELAY,
                            CONNECTION_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An error occurred while scheduling the connection Job");
                }

            }

            if (pollingJob == null || pollingJob.isCancelled()) {
                try {
                    pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, INITIAL_DELAY,
                            ((BigDecimal) getConfig().get(POLLING_REFRESH_INTERVAL)).intValue(), TimeUnit.MILLISECONDS);

                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An error occurred while scheduling the connection Job");
                }
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "IP address or port number not set");
        }

    }

    @Override
    public void dispose() {
        try {
            selector.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the selector: '{}'", e.getMessage());
        }

        try {
            datagramChannel.close();
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", datagramChannel, e.getMessage());
        }

        try {
            listenerChannel.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the listener channel on port number {} ({})",
                    LISTENER_PORT_NUMBER, e.getMessage());
        }

        if (listeningJob != null && !listeningJob.isCancelled()) {
            listeningJob.cancel(true);
            listeningJob = null;
        }

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        logger.debug("Handler disposed.");
    }

    /**
     * creates a listener Channel given a port number (listenerPort)
     *
     * @param listenerPort
     */
    protected void createListenerChannel(int listenerPort) {

        // opening the listener port which can receive packets on UDP port listenerPort passed to the function
        try {
            listenerChannel = DatagramChannel.open();
            // used to establish an association between the socket and a local address
            // once the association is established the socket remains bound until the socket is closed
            listenerChannel.bind(new InetSocketAddress(listenerPort));
            // The Channel must be in non-blocking mode to be used with a Selector
            listenerChannel.configureBlocking(false);

            logger.info("Listening for incoming data on {}", listenerChannel.getLocalAddress());

            synchronized (selector) {
                selector.wakeup();

                try {
                    // Registers this channel with the given selector, returning a selection key
                    // ".validOps" returns an operation set identifying this channel's supported operations
                    listenerKey = listenerChannel.register(selector, listenerChannel.validOps());
                } catch (ClosedChannelException e1) {
                    // TODO: handle exception
                    logger.error("An error occured while registering the selector: {}", e1.getMessage());
                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("An error occured while creating the Listener Channel on port number {} ({})", listenerPort,
                    e.getMessage());
        }

    }

    /**
     * Reads a buffer from the channel and returns it
     * A buffer is essentially a block of memory into which you can write data, which
     * you can then later read again
     *
     * @param theChannel
     * @param bufferSize
     * @param permittedClientAddress
     * @return
     */
    protected ByteBuffer Reader(DatagramChannel theChannel, int bufferSize, InetAddress permittedClientAddress) {
        // The lock() method locks the Lock instance so that all threads calling lock() are blocked until unlock() is
        // executed.
        lock.lock();
        try {
            // retrieves the channel's key representing its registration with the selector
            SelectionKey theSelectionKey = theChannel.keyFor(selector);
            if (theSelectionKey != null) {
                synchronized (selector) {
                    try {
                        // it selects a set of keys whose corresponding channels are ready for I/O operations.
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occured while selecting: {}", e.getMessage());
                    } catch (ClosedSelectorException e) {
                        logger.error("An exception occured while selecting: {}", e.getMessage());
                    }
                }

                // to iterate over the this selector's selected key set
                Iterator<SelectionKey> iterate = selector.selectedKeys().iterator();
                // if iterate has more elements
                while (iterate.hasNext()) {
                    // represents the key representing the channel's registration with the Selector (selector).
                    SelectionKey selectKey = iterate.next();
                    iterate.remove();
                    if (selectKey.isValid() && selectKey.isReadable() && selectKey == theSelectionKey) {
                        // allocate a new byte buffer with 1024 bytes capacity
                        ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                        int numOfBytesRead = 0;
                        boolean error = false;

                        // if the current select key is the key representing the listener's channel registration
                        // with the selector, then read the byte buffer or data from the channel
                        if (selectKey == listenerKey) {
                            try {
                                // receive a datagram via this channel
                                // the channel writes data into the the readBuffer
                                InetSocketAddress clientAddress = (InetSocketAddress) theChannel.receive(readBuffer);
                                // if the returned address given by the receive() is == permitted address
                                if (clientAddress.getAddress().equals(permittedClientAddress)) {
                                    logger.debug("Received {} on the listener port from {}",
                                            new String(readBuffer.array()), clientAddress);
                                    // returns the buffer's position to help as check whether the buffer is
                                    // full or not
                                    numOfBytesRead = readBuffer.position();
                                } else {
                                    logger.warn(
                                            "Received data from '{}' which is not the permitted remote address '{}'",
                                            clientAddress, permittedClientAddress);
                                    // since it is not a permitted remote address return nothing
                                    return null;
                                }

                            } catch (Exception e) {
                                logger.error("An exception occurred while receiving data on the listener port: '{}'",
                                        e.getMessage());
                                error = true;
                            }
                            // if the selectKey != listenerKey
                        } else {

                            try {
                                // reads a datagram from this channel though the selectKey != listenerKey
                                // reads a data from this channel into the readBuffer or
                                // the channel writes data into the the buffer
                                numOfBytesRead = theChannel.read(readBuffer);
                            } catch (NotYetConnectedException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "The MART adapter is not yet connected");
                                error = true;
                            } catch (PortUnreachableException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "This is probably not a MART adapter");
                                error = true;
                            } catch (IOException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "An IO exception occurred");
                                error = true;
                            }
                        }

                        // if numOfBytesRead == -1 then the channel has reached end of stream
                        if (numOfBytesRead == -1) {
                            error = true;
                        }
                        // if error == true , close the channel and re-establish connection
                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error",
                                    getThing().getUID().toString());
                            try {
                                // close the channel
                                theChannel.close();
                            } catch (IOException e) {
                                logger.error("An exception occurred while closing the channel '{}': {}",
                                        datagramChannel, e.getMessage());
                            }

                            // re-establish connection
                            onConnectionLost();
                            // if error == false,
                        } else {
                            // switch the buffer from writing mode into reading mode and return it
                            readBuffer.flip();
                            return readBuffer;

                        }
                    }
                }
            }
            return null;

        } finally {
            lock.unlock();
        }
    }

    /**
     * this function parses the responses or data read from the datagram channel
     *
     * @param byteBuffer
     * @param datagramChannel
     */
    protected void readerHandler(ByteBuffer byteBuffer, DatagramChannel datagramChannel) {
        // Constructs a new String by decoding the specified subarray of bytes using
        // the platform's default charset. The length of the new String is a function
        // of the charset, and hence may not be equal to the length of the subarray
        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());

        // Removes one newline from end of a String if it's there, otherwise leave it alone.
        response = StringUtils.chomp(response);

        // get data to update channels and to send to machine learning web-service
        try {
            JsonObject readData = parser.parse(response).getAsJsonObject();

            for (Entry<String, JsonElement> data : readData.entrySet()) {
                switch (data.getKey()) {
                    case "onToday":
                        State onToday = new DecimalType(data.getValue().getAsInt());
                        if (onToday != null) {
                            logger.debug("", onToday, getThing().getUID());
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ON_TODAY), onToday);
                        }
                        break;

                    case "onTotal":
                        State onTotal = new DecimalType(data.getValue().getAsInt());
                        if (onTotal != null) {
                            logger.debug("", onTotal, getThing().getUID());
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ON_TOTAL), onTotal);
                        }
                        break;

                    default:
                        break;
                }
            }

        } catch (JsonParseException e) {
            logger.debug("Invalid JSON response: '{}'", response);
        }

    }

    /**
     * this function reads from a buffer and writes into a channel
     * A buffer is essentially a block of memory into which you can write data, which
     * you can then later read again
     *
     * @param buffer
     * @param theChannel
     */
    protected void writer(ByteBuffer buffer, DatagramChannel theChannel) {
        lock.lock();

        try {
            // represents the key representing the channel's registration with the Selector (selector).
            SelectionKey theSelectionKey = theChannel.keyFor(selector);

            // check if the key isn't null
            if (theSelectionKey != null) {
                synchronized (selector) {
                    try {
                        // selects a set of keys whose corresponding channels are ready
                        // for I/O operations
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occured while selecting {}", e.getMessage());
                    }
                }

                // returns this selector's selected-key set
                Iterator<SelectionKey> iterate = selector.selectedKeys().iterator();
                while (iterate.hasNext()) {
                    SelectionKey selectKey = iterate.next();
                    iterate.remove();
                    // checks if the key is valid
                    // tests whether this channels is ready for writing
                    // checks whether the current select key is equal to the channel's registered key
                    // with the selector
                    if (selectKey.isValid() && selectKey.isWritable() && selectKey == theSelectionKey) {

                        boolean error = false;
                        // sets the position back to 0, so you can reread all the data in the buffer
                        buffer.rewind();

                        try {
                            logger.debug("Sending '{}' in the channel '{}'->'{}'",
                                    new Object[] { new String(buffer.array()), theChannel.getLocalAddress(),
                                            theChannel.getRemoteAddress() });
                            // www.businessdictionary.com/definition/datagram.html
                            // writes a datagram to this channel
                            theChannel.write(buffer);
                        } catch (NotYetConnectedException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The remote host is not yet connected");
                            error = true;
                        } catch (ClosedChannelException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The connection to the remote host is closed");
                            error = true;
                        } catch (IOException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "An Io exception occurred");
                            error = true;
                        }

                        if (error) {
                            try {
                                // closes the channel if it hasn't being closed already
                                theChannel.close();
                            } catch (Exception e) {
                                logger.warn("An exception occured while closing the channel '{}': {}", datagramChannel,
                                        e.getMessage());
                            }
                        }

                        // re-establish connection
                        onConnectionLost();

                    }
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * establishes a connection for reading and writing
     */
    private void establishConnection() {
        lock.lock();

        try {
            // returns the thing which belongs to the handler
            // gets the status information of the thing
            // gets the detail of the status and checks if there are no configuration errors
            // and checks if the ipAddress is not null or empty
            if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR
                    && getConfig().get(IP_ADDRESS) != null || getConfig().get(IP_ADDRESS) != "") {

                try {
                    // opens a datagram channel
                    datagramChannel = DatagramChannel.open();
                } catch (Exception e) {
                    // updates the status of the thing
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while creating a datagram channel");
                }

                try {
                    // The Channel must be in non-blocking mode to be used with a Selector
                    datagramChannel.configureBlocking(false);

                } catch (IOException e) {
                    // updates the status of the thing
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An error occurred while creating a datagram channel");
                }

                synchronized (selector) {
                    selector.wakeup();

                    // The interest set is the set of events you are interested in "selecting"
                    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

                    try {
                        // registers this channel with the given selector, returning a selectionKey
                        datagramChannelKey = datagramChannel.register(selector, interestSet);

                    } catch (ClosedChannelException e) {
                        // updates the status of the thing
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occured while registering a channel");
                    }

                    // creating an IP Socket Address (remoteAddress) for the remote port number
                    InetSocketAddress remoteAddress = new InetSocketAddress((String) getConfig().get(IP_ADDRESS),
                            REMOTE_PORT_NUMBER);

                    try {
                        logger.trace("Connecting to the channel for {}", remoteAddress);
                        // connects this channel's socket to the given remote peer address
                        datagramChannel.connect(remoteAddress);

                        // after connection update the status of the thing to Online
                        onConnectionResumed();

                    } catch (Exception e) {
                        logger.error("An error occured while connecting connecting to '{}:{}' : {}",
                                new Object[] { (String) getConfig().get(IP_ADDRESS) }, REMOTE_PORT_NUMBER,
                                e.getMessage());

                        // update the status of the thing
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                " An excepton ocurred while connecting");

                    }
                }

            } else {
                // update the status of the thing
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        getThing().getStatusInfo().getDescription());

            }

        } finally {
            lock.unlock();
        }

    }

    /**
     * sends command to the MART adapter
     *
     * @param command
     */
    private void sendMartCommand(String command) {

        if (command != null) {
            // allocate a new buffer with the command's capacity
            ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);

            try {
                byteBuffer.put(command.getBytes("ASCII"));
                writer(byteBuffer, datagramChannel);
            } catch (UnsupportedEncodingException | NumberFormatException e) {
                logger.debug("Exception occurred while sending a command to the MART Adapter '{}':{}",
                        getThing().getUID(), e.getMessage());
            }
        }
    }

    /**
     * this method updates the status of the thing to online
     */
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * establishes a connection if connection is lost
     */
    public void onConnectionLost() {
        establishConnection();
    }

    /**
     * runnable is a task to be executed by a thread
     * the first instruction to be executed is to check if the channel's (datagramChannel) socket is connected for
     * communication
     * the second instruction to be executed is to read from the channel (datagramChannel)
     */
    private Runnable listeningRunnable = new Runnable() {

        @Override
        public void run() {
            lock.lock();

            try {
                // if there are no configuration issue of the thing. if there is an issue it prevents communication
                // with the represented thing and therefore the thing must be reconfigured
                if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
                    // check if the channel for communication with the represented thing is established
                    // check if the channel's socket is connected
                    // otherwise update the status of the thing to make sure it is offline and re-establish
                    // the channel for the communication
                    if (datagramChannel == null || !datagramChannel.isConnected()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "The connection is not yet established");
                    }

                    // if the channel's socket is connected
                    if (datagramChannel.isConnected()) {
                        long stamp = System.currentTimeMillis();
                        // test whether the ip address is reachable in 3 seconds if it is reachable in the timeout
                        // specified, read from the channel
                        if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS))).isReachable(PING_TIME_OUT)) {
                            logger.debug("Ping time out after '{}' milliseconds", System.currentTimeMillis() - stamp);
                            logger.trace("Disconnecting datagram channel '{}'", datagramChannel);
                            try {
                                // close the channel
                                datagramChannel.close();

                            } catch (IOException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "An error occurred while closing the channel");
                            }

                            // if the channel was closed update the thing status
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Ping timeout occurred");
                            // re-establish the connection
                            onConnectionLost();

                        } else {
                            // if the datagram channel is connected, read from the datagram-channel
                            ByteBuffer buffer = Reader(datagramChannel, BUFFER_SIZE, null);
                            // if the buffer is not null and
                            // the number of elements in the buffer between the current position and
                            // the limit is greater than zero
                            if (buffer != null && buffer.remaining() > 0) {
                                // parse the data read from the datagram-channel
                                readerHandler(buffer, datagramChannel);
                            }
                        }
                    }
                    ByteBuffer buffer = Reader(listenerChannel, BUFFER_SIZE,
                            InetAddress.getByName((String) getConfig().get(IP_ADDRESS)));
                    if (buffer != null && buffer.remaining() > 0) {
                        readerHandler(buffer, listenerChannel);
                    }
                }

            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while receiving data from the MART adapter");

            } finally {
                lock.unlock();
            }
        }
    };

    /**
     * runnable is a task to be executed by a thread
     * the task performed here is to write to the channel
     */
    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                String requestUpdate = "Adapter Update";

                // create a byte buffer and allocate a capacity
                ByteBuffer byteBuffer = ByteBuffer.allocate(requestUpdate.getBytes().length);
                try {
                    // transfers the entire content of the byte array into the byteBuffer
                    byteBuffer.put(requestUpdate.getBytes("ASCII"));
                    writer(byteBuffer, datagramChannel);
                } catch (UnsupportedEncodingException | NumberFormatException e) {
                    logger.error("An exception occurred while polling the MART adapter for '{}': {}",
                            getThing().getUID(), e.getMessage());
                }

                requestUpdate = "Fridge Update";
                byteBuffer = ByteBuffer.allocate(requestUpdate.getBytes().length);
                try {
                    byteBuffer.put(requestUpdate.getBytes("ASCII"));
                    writer(byteBuffer, datagramChannel);

                } catch (UnsupportedEncodingException | NumberFormatException e) {

                }

                requestUpdate = "Television Update";
                byteBuffer = ByteBuffer.allocate(requestUpdate.getBytes().length);
                try {
                    byteBuffer.put(requestUpdate.getBytes("ASCII"));
                    writer(byteBuffer, datagramChannel);

                } catch (UnsupportedEncodingException | NumberFormatException e) {

                }

                requestUpdate = "Outside Update";
                byteBuffer = ByteBuffer.allocate(requestUpdate.getBytes().length);
                try {
                    byteBuffer = ByteBuffer.allocate(requestUpdate.getBytes("ASCII").length);
                    writer(byteBuffer, datagramChannel);

                } catch (UnsupportedEncodingException | NumberFormatException e) {

                }

            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    };

    /**
     * post device usage data to the MART web-service
     */
    private void postman() throws Exception {
        String endpoints = "";
        URL url = new URL(endpoints);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

        // add request header

        // send post request

    }
}
