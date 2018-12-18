package org.araymond.joal.core.ttorrent.client;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * Created by raymo on 16/07/2017.
 */
public class ConnectionHandlerTest {

    public static ConnectionHandler createMockedIpv4(final int port) {
        try {
            final InetAddress inetAddress = InetAddress.getByName("123.123.123.123");
            final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
            Mockito.when(connectionHandler.getPort()).thenReturn(port);
            Mockito.when(connectionHandler.getIpAddress()).thenReturn(inetAddress);
            return connectionHandler;
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ConnectionHandler createMockedIpv6(final int port) {
        try {
            final InetAddress inetAddress = InetAddress.getByName("fd2d:7212:4cd5:2f14:ffff:ffff:ffff:ffff");;
            final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
            Mockito.when(connectionHandler.getPort()).thenReturn(46582);
            Mockito.when(connectionHandler.getIpAddress()).thenReturn(inetAddress);
            return connectionHandler;
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ServerSocketChannel createMockedServerSocketChannel(final int port) {
        final ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
        Mockito.when(serverSocket.getLocalPort()).thenReturn(port);
        final ServerSocketChannel serverSocketChannel = Mockito.mock(ServerSocketChannel.class);
        Mockito.when(serverSocketChannel.socket()).thenReturn(serverSocket);
        return serverSocketChannel;
    }

    @Test
    public void shouldFallbackToLocalhostIfFailedToFetchAndNoIpWereNeverFetched() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(49152);

        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doReturn(Optional.empty()).when(handler).tryToFetchFromProviders();
        doReturn(channel).when(handler).bindToPort();

        handler.start();
        assertThat(handler.getIpAddress()).isEqualTo(InetAddress.getLocalHost());
    }

    @Test
    public void shouldUseFetchedIpIfSuccessful() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(49152);
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doReturn(channel).when(handler).bindToPort();
        doReturn(Optional.of(InetAddress.getByName("168.168.168.168"))).when(handler).tryToFetchFromProviders();

        handler.start();

        assertThat(handler.getIpAddress().getHostAddress()).isEqualTo("168.168.168.168");
    }

    @Test
    public void shouldFillPortAndIpOnInit() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(65534);
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doReturn(channel).when(handler).bindToPort();
        doReturn(Optional.of(InetAddress.getByName("168.168.168.168"))).when(handler).tryToFetchFromProviders();

        handler.start();

        assertThat(handler.getIpAddress().getHostAddress()).isEqualTo("168.168.168.168");
        assertThat(handler.getPort()).isEqualTo(65534);
    }

    @Test
    public void shouldBindToPort() {
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = new ConnectionHandler().bindToPort();
            assertThat(serverSocketChannel.socket().getLocalPort()).isBetween(ConnectionHandler.PORT_RANGE_START, ConnectionHandler.PORT_RANGE_END);
            serverSocketChannel.close();
        } catch (final IOException e) {
            fail("should not have failed", e);
        } finally {
            try {
                if (serverSocketChannel != null) serverSocketChannel.close();
            } catch (final IOException ignored) {
            }
        }
    }

    @Test
    public void shouldNotFailToBindToPortIfFirstPortIsNotAvailable() {
        ServerSocket serverSocket = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocket = new ServerSocket(ConnectionHandler.PORT_RANGE_START);
            serverSocketChannel = new ConnectionHandler().bindToPort();
            assertThat(serverSocketChannel.socket().getLocalPort()).isBetween(ConnectionHandler.PORT_RANGE_START, ConnectionHandler.PORT_RANGE_END);
        } catch (final IOException e) {
            fail("should not have failed", e);
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (final IOException ignored) {
            }
            try {
                if (serverSocketChannel != null) serverSocketChannel.close();
            } catch (final IOException ignored) {
            }
        }
    }

    @Test
    public void shouldCallProvidersOneByOneUntilOneReturnsIp() throws IOException {
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doThrow(new IOException("hehe :) you won't get it on first try"))
                .doReturn(InetAddress.getByName("168.168.168.168"))
                .when(handler).readIpFromProvider(ArgumentMatchers.anyString());

        handler.fetchIp();

        Mockito.verify(handler, Mockito.times(2)).readIpFromProvider(ArgumentMatchers.anyString());
    }

    @Test
    public void shouldFallBackToLocalHostIfNoProviderAreReachable() throws IOException {
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doThrow(new IOException("hehe :) you won't get it on first try"))
                .when(handler).readIpFromProvider(ArgumentMatchers.anyString());

        assertThat(handler.fetchIp()).isEqualTo(InetAddress.getLocalHost());
        Mockito.verify(handler, Mockito.atLeast(1)).readIpFromProvider(ArgumentMatchers.anyString());
    }

    @Test
    public void shouldReuseLastKnownAddressIfOneWasAlreadyGotAndNoProviderAreReachable() throws IOException {
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doReturn(InetAddress.getByName("168.168.168.168"))
                .when(handler).readIpFromProvider(ArgumentMatchers.anyString());
        handler.start();
        Mockito.verify(handler, Mockito.atLeast(1)).readIpFromProvider(ArgumentMatchers.anyString());

        doThrow(new IOException("hehe :) you won't get it on first try"))
                .when(handler).readIpFromProvider(ArgumentMatchers.anyString());
        assertThat(handler.fetchIp()).isEqualTo(InetAddress.getByName("168.168.168.168"));
    }

    @Test
    public void shouldCloseChannelOnStop() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(49152);

        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        doReturn(Optional.empty()).when(handler).tryToFetchFromProviders();
        doReturn(channel).when(handler).bindToPort();

        handler.start();
        handler.close();
    }

}
