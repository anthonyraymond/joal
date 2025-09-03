package org.araymond.joal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

@Profile("!test")
@Component
@Slf4j
public class TrayIconManager implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {
    private final ConfigurableApplicationContext applicationContext;
    private TrayIcon trayIcon;

    @Inject
    public TrayIconManager(final ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (!SystemTray.isSupported()) {
            log.warn("System tray not supported on this environment");
            return;
        }

        try {
            final SystemTray tray = SystemTray.getSystemTray();

            final PopupMenu popup = new PopupMenu();
            final MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> applicationContext.close());
            popup.add(exitItem);

            final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 16, 16);
            g.setColor(Color.WHITE);
            g.drawString("J", 4, 12);
            g.dispose();

            trayIcon = new TrayIcon(image, "JOAL", popup);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        applicationContext.close();
                    }
                }
            });
            tray.add(trayIcon);
        } catch (final Exception e) {
            log.warn("Failed to initialise system tray", e);
        }
    }

    @Override
    public void destroy() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
