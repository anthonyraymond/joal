package org.araymond.joal.core.ttorent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by raymo on 23/01/2017.
 */
public class ConnectionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    public static final int PORT_RANGE_START = 49152;
    public static final int PORT_RANGE_END = 65534;

    private final MockedTorrent torrent;
    private final String id;
    private ServerSocketChannel channel;
    private InetSocketAddress address;

    /**
     * Create and start a new listening service for out torrent, reporting
     * with our peer ID on the given address.
     * <p>
     * <p>
     * This binds to the first available port in the client port range
     * PORT_RANGE_START to PORT_RANGE_END.
     * </p>
     *
     * @param torrent The torrent shared by this client.
     * @param id      This client's peer ID.
     * @param address The address to bind to.
     * @throws IOException When the service can't be started because no port in
     *                     the defined range is available or usable.
     */
    ConnectionHandler(final MockedTorrent torrent, final String id, final InetAddress address) throws IOException {
        this.torrent = torrent;
        this.id = id;

        // Bind to the first available port in the range
        // [PORT_RANGE_START; PORT_RANGE_END].
        for (int port = ConnectionHandler.PORT_RANGE_START; port <= ConnectionHandler.PORT_RANGE_END; port++) {
            final InetSocketAddress tryAddress = new InetSocketAddress(address, port);

            try {
                this.channel = ServerSocketChannel.open();
                this.channel.socket().bind(tryAddress);
                this.channel.configureBlocking(false);
                this.address = tryAddress;
                break;
            } catch (final IOException ioe) {
                // Ignore, try next port
                logger.warn("Could not bind to {}, trying next port...", tryAddress);
            }
        }

        if (this.channel == null || !this.channel.socket().isBound()) {
            throw new IOException("No available port for the BitTorrent client!");
        }

        logger.info("Listening for incoming connections on {}.", this.address);
    }

    /**
     * Return the full socket address this service is bound to.
     */
    public InetSocketAddress getSocketAddress() {
        return this.address;
    }

    public void close() throws IOException {
        logger.trace("Call to close ConnectionHandler.");
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
        logger.trace("ConnectionHandler closed.");
    }

}
