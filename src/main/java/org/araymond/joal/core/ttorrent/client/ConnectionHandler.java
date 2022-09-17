package org.araymond.joal.core.ttorrent.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * This class has 2 main functions:
 * <ul>
 *     <li>establishes a socket at a port that accepts connections</li>
 *     <li>periodically resolves our external IP address</li>
 * </ul>
 * Note this port & IP will be reported back to trackers via announcements, if your client
 * {@code query} contains relevant placeholder(s).
 * <p/>
 * Created by raymo on 23/01/2017.
 */
@Slf4j
public class ConnectionHandler {

    public static final int PORT_RANGE_START = 49152;
    public static final int PORT_RANGE_END = 65534;

    private ServerSocketChannel channel;
    @Getter private InetAddress ipAddress;
    private Thread ipFetcherThread;
    private static final String[] IP_PROVIDERS = new String[]{
            "http://whatismyip.akamai.com",
            "http://ipecho.net/plain",
            "http://ip.tyk.nu/",
            "http://l2.io/ip",
            "http://ident.me/",
            "http://icanhazip.com/",
            "https://api.ipify.org",
            "https://ipinfo.io/ip",
            "https://checkip.amazonaws.com"
    };

    public int getPort() {
        return this.channel.socket().getLocalPort();
    }

    public void start() throws IOException {
        this.channel = this.bindToPort();
        final int port = this.channel.socket().getLocalPort();
        log.info("Listening for incoming peer connections on port {}", port);

        this.ipAddress = fetchIp();
        log.info("IP reported to tracker will be: {}", this.getIpAddress().getHostAddress());

        // TODO: use @Scheduled
        this.ipFetcherThread = new Thread(() -> {
            while (this.ipFetcherThread == null || !this.ipFetcherThread.isInterrupted()) {
                try {
                    MINUTES.sleep(90);  // TODO: move to config
                    this.ipAddress = this.fetchIp();
                } catch (final UnknownHostException e) {
                    log.warn("Failed to fetch external IP", e);
                } catch (final InterruptedException e) {
                    log.info("IP fetcher thread has been stopped");
                }
            }
        });

        this.ipFetcherThread.start();
    }

    @VisibleForTesting
    InetAddress readIpFromProvider(final String providerUrl) throws IOException {
        final URLConnection urlConnection = new URL(providerUrl).openConnection();
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");  // TODO: move to config
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charsets.UTF_8))) {
            return InetAddress.getByName(in.readLine());
        } finally {
            // Ensure all streams associated with http connection are closed
            final InputStream errStream = ((HttpURLConnection) urlConnection).getErrorStream();
            try { if (errStream != null) errStream.close(); }
            catch (final IOException ignored) {}
        }
    }

    @VisibleForTesting
    Optional<InetAddress> tryToFetchFromProviders() {
        final List<String> shuffledList = Arrays.asList(IP_PROVIDERS);
        Collections.shuffle(shuffledList);  // TODO: why shuffle? perhaps better use Iterators.cycle here like we do in TrackerClientUriProvider?

        for (final String ipProviderUrl : shuffledList) {
            log.info("Fetching ip from {}", ipProviderUrl);
            try {
                return Optional.of(this.readIpFromProvider(ipProviderUrl));
            } catch (final IOException e) {
                log.warn("Failed to fetch IP from [" + ipProviderUrl + "]", e);
            }
        }

        return Optional.empty();
    }

    @VisibleForTesting
    InetAddress fetchIp() throws UnknownHostException {
        final Optional<InetAddress> ip = this.tryToFetchFromProviders();
        if (ip.isPresent()) {
            log.info("Successfully fetched public IP address: [{}]", ip.get().getHostAddress());
            return ip.get();
        } else if (this.ipAddress != null) {
            log.warn("Failed to fetch public IP address, reusing last known IP address: [{}]", this.ipAddress.getHostAddress());
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
                log.warn("Could not bind to port {}: {}", tryAddress.getPort(), ioe.getMessage());
                log.warn("trying next port...");
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
        log.debug("Closing ConnectionHandler...");
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        } catch (final Exception e) {
            log.warn("ConnectionHandler channel has failed to release channel, but the shutdown will proceed", e);
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
        log.debug("ConnectionHandler closed");
    }
}
