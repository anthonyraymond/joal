package org.araymond.joal.core.ttorent.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.Optional;

/**
 * Created by raymo on 23/01/2017.
 */
public class ConnectionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    public static final int PORT_RANGE_START = 49152;
    public static final int PORT_RANGE_END = 65534;

    private ServerSocketChannel channel;
    private InetAddress ipAddress;
    private Thread ipFetcherThread;


    public ConnectionHandler() {
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    public int getPort() {
        return this.channel.socket().getLocalPort();
    }

    public void init() throws IOException {
        this.channel = this.bindToPort();
        final int port = this.channel.socket().getLocalPort();
        logger.info("Listening for incoming peer connections on port {}.", port);

        this.ipAddress = fetchIp();
        logger.info("Ip reported to tracker will be: {}", this.getIpAddress().getHostAddress());

        this.ipFetcherThread = new Thread(() -> {
            try {
                // Sleep for one hour and a half.
                Thread.sleep(1000 * 5400);
                this.ipAddress = this.fetchIp();
            } catch (final UnknownHostException ignored) {
            } catch (final InterruptedException e) {
                logger.info("Ip fetcher thread has been stopped.");
            }
        });
    }

    @VisibleForTesting
    Optional<InetAddress> getWtfIsMyIp() {
        final String ip;
        try {
            final URL url = new URL("https://wtfismyip.com/json");

            ip = new ObjectMapper().readValue(url, FuckingIpAddressPayload.class).getYourFuckingIPAddress();
            return Optional.of(InetAddress.getByName(ip));
        } catch (final IOException e) {
            logger.warn("Failed to fetch Ip", e);
            return Optional.empty();
        }
    }

    @VisibleForTesting
    InetAddress fetchIp() throws UnknownHostException {
        final Optional<InetAddress> ip = this.getWtfIsMyIp();
        if (ip.isPresent()) {
            logger.info("Successfully fetch public IP address: {}", ip.get().getHostAddress());
            return ip.get();
        }
        if (this.ipAddress != null) {
            logger.warn("Failed to fetch public IP address, reuse last known IP address: {}", this.ipAddress.getHostAddress());
            return this.ipAddress;
        }
        logger.warn("Failed to fetch public IP address, fallback to localhost");
        return InetAddress.getLocalHost();
    }

    @VisibleForTesting
    ServerSocketChannel bindToPort() throws IOException {
        // Bind to the first available port in the range
        ServerSocketChannel channel = null;

        for (int port = ConnectionHandler.PORT_RANGE_START; port <= ConnectionHandler.PORT_RANGE_END; port++) {
            final InetSocketAddress tryAddress = new InetSocketAddress(InetAddress.getLocalHost(), port);


            try {
                channel = ServerSocketChannel.open();
                channel.socket().bind(tryAddress);
                channel.configureBlocking(false);
                break;
            } catch (final IOException ioe) {
                // Ignore, try next port
                logger.warn("Could not bind to {}, trying next port...", tryAddress);
                try {
                    if (channel != null) channel.close();
                } catch (final IOException ignored) {
                }
            }
        }

        if (channel == null || !channel.socket().isBound()) {
            throw new IOException("No available port for the BitTorrent client!");
        }
        return channel;
    }

    public void close() throws IOException {
        logger.debug("Call to close ConnectionHandler.");
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
        if (this.ipFetcherThread != null) {
            this.ipFetcherThread.interrupt();
            this.ipFetcherThread = null;
        }
        logger.debug("ConnectionHandler closed.");
    }

    @SuppressWarnings("unused")
    private static final class FuckingIpAddressPayload {

        private final String yourFuckingIPAddress;
        private final String yourFuckingLocation;
        private final String yourFuckingHostname;
        private final String yourFuckingISP;
        private final String yourFuckingTorExit;

        @JsonCreator
        private FuckingIpAddressPayload(
                @JsonProperty(value = "YourFuckingIPAddress", required = true) final String yourFuckingIPAddress,
                @JsonProperty("YourFuckingLocation") final String yourFuckingLocation,
                @JsonProperty("YourFuckingHostname") final String yourFuckingHostname,
                @JsonProperty("YourFuckingISP") final String yourFuckingISP,
                @JsonProperty("YourFuckingTorExit") final String yourFuckingTorExit) {
            this.yourFuckingIPAddress = yourFuckingIPAddress;
            this.yourFuckingLocation = yourFuckingLocation;
            this.yourFuckingHostname = yourFuckingHostname;
            this.yourFuckingISP = yourFuckingISP;
            this.yourFuckingTorExit = yourFuckingTorExit;
        }

        public String getYourFuckingIPAddress() {
            return yourFuckingIPAddress;
        }
    }

}
