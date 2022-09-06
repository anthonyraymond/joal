package org.araymond.joal.core.ttorrent.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by raymo on 23/01/2017.
 */
@Slf4j
public class ConnectionHandler {

    public static final int PORT_RANGE_START = 49152;
    public static final int PORT_RANGE_END = 65534;

    private ServerSocketChannel channel;
    @Getter
    private InetAddress ipAddress;
    private Thread ipFetcherThread;
    private static final String[] IP_PROVIDERS = new String[]{
            "http://ip.tyk.nu/",
            "http://l2.io/ip",
            "http://ident.me/",
            "http://icanhazip.com/"
    };

    public int getPort() {
        return this.channel.socket().getLocalPort();
    }

    public void start() throws IOException {
        this.channel = this.bindToPort();
        final int port = this.channel.socket().getLocalPort();
        log.info("Listening for incoming peer connections on port {}.", port);

        this.ipAddress = fetchIp();
        log.info("Ip reported to tracker will be: {}", this.getIpAddress().getHostAddress());

        this.ipFetcherThread = new Thread(() -> {
            while (this.ipFetcherThread == null || !this.ipFetcherThread.isInterrupted()) {
                try {
                    // Sleep for one hour and a half.
                    Thread.sleep(1000 * 5400);
                    this.ipAddress = this.fetchIp();
                } catch (final UnknownHostException e) {
                    log.warn("Faield to fetch Ip", e);
                } catch (final InterruptedException e) {
                    log.info("Ip fetcher thread has been stopped.");
                }
            }
        });

        this.ipFetcherThread.start();
    }

    @VisibleForTesting
    InetAddress readIpFromProvider(final String providerUrl) throws IOException {
        final URLConnection urlConnection = new URL(providerUrl).openConnection();
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charsets.UTF_8))) {
            return InetAddress.getByName(in.readLine());
        } finally {
            // Ensure all streams associated with http connection are closed
            final InputStream err = ((HttpURLConnection) urlConnection).getErrorStream();
            try { if (err != null) err.close(); }
            catch (final IOException ignored) {}
        }
    }

    @VisibleForTesting
    Optional<InetAddress> tryToFetchFromProviders() {
        final List<String> shuffledList = Lists.newArrayList(IP_PROVIDERS);
        Collections.shuffle(shuffledList);

        for (final String ipProvider : shuffledList) {
            log.info("Fetching ip from: " + ipProvider);
            try {
                return Optional.of(this.readIpFromProvider(ipProvider));
            } catch (final IOException e) {
                log.warn("Failed to fetch Ip from [" + ipProvider + "]", e);
            }
        }

        return Optional.empty();
    }

    @VisibleForTesting
    InetAddress fetchIp() throws UnknownHostException {
        final Optional<InetAddress> ip = this.tryToFetchFromProviders();
        if (ip.isPresent()) {
            log.info("Successfully fetch public IP address: {}", ip.get().getHostAddress());
            return ip.get();
        }
        if (this.ipAddress != null) {
            log.warn("Failed to fetch public IP address, reuse last known IP address: {}", this.ipAddress.getHostAddress());
            return this.ipAddress;
        }
        log.warn("Failed to fetch public IP address, fallback to localhost");
        return InetAddress.getLocalHost();
    }

    @VisibleForTesting
    ServerSocketChannel bindToPort() throws IOException {
        // Bind to the first available port in the range
        ServerSocketChannel channel = null;

        for (int port = ConnectionHandler.PORT_RANGE_START; port <= ConnectionHandler.PORT_RANGE_END; port++) {
            final InetSocketAddress tryAddress = new InetSocketAddress(port);

            try {
                channel = ServerSocketChannel.open();
                channel.socket().bind(tryAddress);
                channel.configureBlocking(false);
                break;
            } catch (final IOException ioe) {
                // Ignore, try next port
                log.warn("Could not bind to port {}: {}, trying next port...", tryAddress.getPort(), ioe.getMessage());
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

    public void close() {
        log.debug("Call to close ConnectionHandler.");
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        } catch (final Exception e) {
            log.warn("ConnectionHandler channel has failed to release channel, but the shutdown will proceed.", e);
        } finally {
            this.channel = null;
        }
        try {
            if (this.ipFetcherThread != null) {
                this.ipFetcherThread.interrupt();
            }
        } finally {
            this.ipFetcherThread = null;
        }
        log.debug("ConnectionHandler closed.");
    }

}
