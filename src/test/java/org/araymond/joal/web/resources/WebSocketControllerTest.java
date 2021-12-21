package org.araymond.joal.web.resources;

import com.google.common.collect.Lists;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.AppConfigurationTest;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.InfoHashTest;
import org.araymond.joal.core.torrent.torrent.MockedTorrentTest;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.web.messages.incoming.config.Base64TorrentIncomingMessage;
import org.araymond.joal.web.messages.incoming.config.ConfigIncomingMessage;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class WebSocketControllerTest {

    @Test
    public void shouldSaveConfiguration() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        final AppConfiguration config = AppConfigurationTest.createOne();
        final ConfigIncomingMessage configIncomingMessage = mock(ConfigIncomingMessage.class);
        doReturn(config.getMinUploadRate()).when(configIncomingMessage).getMinUploadRate();
        doReturn(config.getMaxUploadRate()).when(configIncomingMessage).getMaxUploadRate();
        doReturn(config.getSimultaneousSeed()).when(configIncomingMessage).getSimultaneousSeed();
        doReturn(config.getClientFileName()).when(configIncomingMessage).getClient();
        doReturn(config.shouldKeepTorrentWithZeroLeechers()).when(configIncomingMessage).shouldKeepTorrentWithZeroLeechers();
        doReturn(config).when(configIncomingMessage).toAppConfiguration();

        controller.saveNewConf(configIncomingMessage);

        Mockito.verify(seedManager, times(1)).saveNewConfiguration(eq(config));
    }

    @Test
    public void shouldNotifyWhenFailToSaveConf() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        final ConfigIncomingMessage configIncomingMessage = mock(ConfigIncomingMessage.class);
        doThrow(new TorrentClientConfigIntegrityException("yep :)")).when(configIncomingMessage).toAppConfiguration();

        controller.saveNewConf(configIncomingMessage);

        Mockito.verify(sendingTemplate, times(1)).convertAndSend(eq("/config"), any(InvalidConfigPayload.class));
    }

    @Test
    public void shouldStartSeeding() throws IOException {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        controller.startStartSession();

        verify(seedManager, times(1)).startSeeding();
    }

    @Test
    public void shouldStopSeeding() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        controller.stopSeedSession();

        verify(seedManager, times(1)).stop();
    }

    @Test
    public void shouldSaveTorrentToDisk() throws IOException {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        final byte[] b64 = org.apache.commons.codec.binary.Base64.encodeBase64("hello".getBytes());
        controller.uploadTorrent(new Base64TorrentIncomingMessage("d", new String(b64)));

        verify(seedManager, times(1)).saveTorrentToDisk(eq("d"), eq(Base64.decodeBase64(b64)));
    }

    @Test
    public void shouldDeleteTorrent() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);
        final InfoHash infoHash = InfoHashTest.createOne("aaa");
        controller.deleteTorrent("aaa");

        verify(seedManager, times(1)).deleteTorrent(infoHash);
    }

    @Test
    public void shouldProvideInitializationListOfEventWhenStarted() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);
        final AnnouncerFacade announcerFacade = mock(AnnouncerFacade.class);
        doReturn(Optional.empty()).when(announcerFacade).getLastAnnouncedAt();
        doReturn(Optional.empty()).when(announcerFacade).getLastKnownLeechers();
        doReturn(Optional.empty()).when(announcerFacade).getLastKnownSeeders();

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        doReturn(Lists.newArrayList("utorrent")).when(seedManager).listClientFiles();
        doReturn(AppConfigurationTest.createOne()).when(seedManager).getCurrentConfig();
        doReturn(Lists.newArrayList(MockedTorrentTest.createOneMock("abc"), MockedTorrentTest.createOneMock("def"))).when(seedManager).getTorrentFiles();
        doReturn(true).when(seedManager).isSeeding();
        doReturn("utorrent").when(seedManager).getCurrentEmulatedClient();
        doReturn(Maps.newHashMap(InfoHashTest.createOne("abc"), mock(Speed.class))).when(seedManager).getSpeedMap();
        doReturn(Lists.newArrayList(announcerFacade)).when(seedManager).getCurrentlySeedingAnnouncer();

        controller.list();

        verify(seedManager, times(1)).listClientFiles();
        verify(seedManager, times(1)).getCurrentConfig();
        verify(seedManager, times(1)).getTorrentFiles();
        verify(seedManager, times(1)).isSeeding();
        verify(seedManager, times(1)).getCurrentEmulatedClient();
        verify(seedManager, times(1)).getSpeedMap();
        verify(seedManager, times(1)).getCurrentlySeedingAnnouncer();
        verifyNoMoreInteractions(seedManager);
    }

    @Test
    public void shouldProvideInitializationListOfEventWhenNotStarted() {
        final SeedManager seedManager = mock(SeedManager.class);
        final JoalMessageSendingTemplate sendingTemplate = mock(JoalMessageSendingTemplate.class);
        final AnnouncerFacade announcerFacade = mock(AnnouncerFacade.class);
        doReturn(Optional.empty()).when(announcerFacade).getLastAnnouncedAt();
        doReturn(Optional.empty()).when(announcerFacade).getLastKnownLeechers();
        doReturn(Optional.empty()).when(announcerFacade).getLastKnownSeeders();

        final WebSocketController controller = new WebSocketController(seedManager, sendingTemplate);

        doReturn(Lists.newArrayList("utorrent")).when(seedManager).listClientFiles();
        doReturn(AppConfigurationTest.createOne()).when(seedManager).getCurrentConfig();
        doReturn(Lists.newArrayList(MockedTorrentTest.createOneMock("abc"), MockedTorrentTest.createOneMock("def"))).when(seedManager).getTorrentFiles();
        doReturn(false).when(seedManager).isSeeding();
        doReturn(Maps.newHashMap(InfoHashTest.createOne("abc"), mock(Speed.class))).when(seedManager).getSpeedMap();
        doReturn(Lists.newArrayList(announcerFacade)).when(seedManager).getCurrentlySeedingAnnouncer();

        controller.list();

        verify(seedManager, times(1)).listClientFiles();
        verify(seedManager, times(1)).getCurrentConfig();
        verify(seedManager, times(1)).getTorrentFiles();
        verify(seedManager, times(1)).isSeeding();
        verify(seedManager, times(1)).getSpeedMap();
        verify(seedManager, times(1)).getCurrentlySeedingAnnouncer();
        verifyNoMoreInteractions(seedManager);
    }
}
