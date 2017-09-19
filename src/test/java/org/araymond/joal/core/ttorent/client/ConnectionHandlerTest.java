package org.araymond.joal.core.ttorent.client;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class ConnectionHandlerTest {

    private static ServerSocketChannel createMockedServerSocketChannel(final int port) throws IOException {
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
        Mockito.doReturn(Optional.empty()).when(handler).tryToFetchFromProviders();
        Mockito.doReturn(channel).when(handler).bindToPort();

        handler.init();
        assertThat(handler.getIpAddress().isSiteLocalAddress()).isTrue();
    }

    @Test
    public void shouldUseLastKnownAddressIfFailedToFetch() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(49152);

        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        Mockito.doReturn(channel).when(handler).bindToPort();

        // emulate successful call
        Mockito.doReturn(Optional.of(InetAddress.getByName("168.168.168.168"))).when(handler).tryToFetchFromProviders();
        handler.init();

        // emulate failed call
        Mockito.doReturn(Optional.empty()).when(handler).tryToFetchFromProviders();
        handler.fetchIp();

        assertThat(handler.getIpAddress().getHostAddress()).isEqualTo("168.168.168.168");
    }

    @Test
    public void shouldUseFetchedIpIfSuccessful() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(49152);
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        Mockito.doReturn(channel).when(handler).bindToPort();
        Mockito.doReturn(Optional.of(InetAddress.getByName("168.168.168.168"))).when(handler).tryToFetchFromProviders();

        handler.init();

        assertThat(handler.getIpAddress().getHostAddress()).isEqualTo("168.168.168.168");
    }

    @Test
    public void shouldFillPortAndIpOnInit() throws IOException {
        final ServerSocketChannel channel = createMockedServerSocketChannel(65534);
        final ConnectionHandler handler = Mockito.spy(new ConnectionHandler());
        Mockito.doReturn(channel).when(handler).bindToPort();
        Mockito.doReturn(Optional.of(InetAddress.getByName("168.168.168.168"))).when(handler).tryToFetchFromProviders();

        handler.init();

        assertThat(handler.getIpAddress().getHostAddress()).isEqualTo("168.168.168.168");
        assertThat(handler.getPort()).isEqualTo(65534);
    }

}
