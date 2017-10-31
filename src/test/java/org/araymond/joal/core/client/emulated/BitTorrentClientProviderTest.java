package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.config.JoalConfigProviderTest;
import org.araymond.joal.core.events.config.ClientFilesDiscoveredEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 23/04/2017.
 */
public class BitTorrentClientProviderTest {

    private static final SeedManager.JoalFoldersPath joalFoldersPath = new SeedManager.JoalFoldersPath(Paths.get("src/test/resources/configtest"));

    private static BitTorrentClientProvider createProvider() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        Mockito.when(configProvider.get()).thenReturn(JoalConfigProviderTest.defaultConfig);
        return new BitTorrentClientProvider(configProvider, new ObjectMapper(), joalFoldersPath, Mockito.mock(ApplicationEventPublisher.class));
    }

    @Test
    public void shouldFailIfClientFileDoesNotExists() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        Mockito.when(configProvider.get()).thenReturn(JoalConfigProviderTest.defaultConfig);
        final BitTorrentClientProvider provider = new BitTorrentClientProvider(configProvider, new ObjectMapper(), new SeedManager.JoalFoldersPath(Paths.get("nop")), Mockito.mock(ApplicationEventPublisher.class));

        assertThatThrownBy(provider::generateNewClient)
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("BitTorrent client configuration file");
    }

    @Test
    public void shouldReturnSameClientEveryTimes() throws FileNotFoundException {
        final BitTorrentClientProvider provider = createProvider();

        assertThat(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get());
    }

    @Test
    public void shouldChangeClientEveryTimesGenerateIsCalled() throws FileNotFoundException {
        final BitTorrentClientProvider provider = createProvider();

        assertThat(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get())
                .isEqualToComparingFieldByField(provider.get());
    }

    @Test
    public void shouldGetClient() throws FileNotFoundException {
        final BitTorrentClientProvider provider = createProvider();

        assertThat(provider.get()).isNotNull();
    }

    @Test
    public void shouldPublishClientFilesDiscoveredOnInit() {
        final ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        final BitTorrentClientProvider provider = new BitTorrentClientProvider(Mockito.mock(JoalConfigProvider.class), new ObjectMapper(), joalFoldersPath, publisher);

        provider.init();

        final ArgumentCaptor<ClientFilesDiscoveredEvent> captor = ArgumentCaptor.forClass(ClientFilesDiscoveredEvent.class);
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(captor.capture());

        final ClientFilesDiscoveredEvent event = captor.getValue();
        assertThat(event.getClients()).hasSize(1);
        assertThat(event.getClients().get(0)).isEqualTo("azureus-5.7.5.0.client");
    }


}
