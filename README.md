# Disclaimer

JOAL is not designed to help or encourage you downloading illegal materials ! You must respect the law applicable in your country. I couldn't be held responsible for illegal activities performed by your usage of JOAL.

# JOAL
Is an open-source tool that simulates a BitTorrent client to manipulate the ratio without real file transfers. I forked the [project](https://github.com/anthonyraymond/joal) to provide a complete version, including the JAR file, configuration, and all necessary files within a Docker image. It serves as the server application (with an optional web UI). For the desktop version, see [here](https://github.com/anthonyraymond/joal-desktop).

## Preview

![preview](readme-assets/webui-preview.png?raw=true)


## Which clients can JOAL emulate?

| Client        | Support                       | Comment        |  | Client        | Support                       | Comment        |
| ------------- |:-----------------------------:|----------------|--|---------------|:-----------------------------:|----------------|
| BitComet      | ![Numwant mess][support-never]| Will never be! |  | Transmission  | ![Yes][support-yes]           |                |
| BitTorrent    | ![Yes][support-yes]           |                |  | µTorrent      | ![Yes][support-yes]           |                |
| Deluge        | ![Yes][support-yes]           |                |  | Vuze Azureus  | ![Yes][support-yes]           |                |
| qBittorrent   | ![Yes][support-yes]           |                |  | Vuze Leap     | ![Yes][support-yes]           |                |
| rTorrent      | ![Yes][support-yes]           |                |  |               |                              |                |

If your favorite client is not yet supported, feel free to ask (except for BitComet).  
Ask for it in GitHub issues or mail <a href="mailto:joal.contact@gmail.com">joal.contact@gmail.com</a>.


## Run with Docker

In the next command, replace `PATH_TO_CONF`, `PORT`, `SECRET_OBFUSCATION_PATH`, and `SECRET_TOKEN` with your desired values.

```bash
docker run -d \
  --name=joal \
  --restart=unless-stopped \
  -v "$(pwd)/data:/data" \
  -p 8080:8080 \
  ghcr.io/skylanix/joal:latest \
  --joal-conf=/data \
  --spring.main.web-environment=true \
  --server.port=8080 \
  --joal.ui.path.prefix=joal \
  --joal.ui.secret-token=YourToken
```

Or the equivalent docker-compose service:

```yaml
services:
  joal:
    image: ghcr.io/skylanix/joal:latest
    container_name: joal
    restart: unless-stopped
    volumes:
      - ./data:/data
    ports:
      - 8080:8080
    command: 
      - "--joal-conf=/data"
      - "--spring.main.web-environment=true"
      - "--server.port=8080"
      - "--joal.ui.path.prefix=joal"
      - "--joal.ui.secret-token=YourToken"
```

**Warnings:**
- ⚠️ Set a strong password for `YourToken` and do not expose the Joal admin page to the public.
- ⚠️ If you are satisfied with your ratio and want to avoid getting banned, stop the simulator by adding a `#` in front of `restart: unless-stopped` in the compose.yml file. This will prevent the container from automatically restarting, as the simulator starts uploading when it launches.

Access the web interface (make sure to add `/joal/ui`):

```
http://ip:8080/joal/ui
```

### Usage steps

1. Click the "Modify connection settings" option.  
   ![Webui change connection settings](readme-assets/webui-change-connection-settings.png?raw=true)
2. On your first login, set up the connection settings:
    - Server address: enter the server’s IP address
    - Server port: 8080
    - Path prefix: joal
    - Secret token: use the token you chose, here "YourToken"
    - Click "Save"  
      ![Webui connection settings](readme-assets/webui-connection-settings.png?raw=true)
3. The "Configuration" tab allows you to set different options:
    - Minimum and maximum upload speeds
    - Your preferred torrent client
    - Maximum number of torrents that can be uploaded simultaneously
    - Required ratio for each torrent (use "-1" for unlimited ratio)
4. It is recommended to be careful with the configuration settings and not to set the upload speeds too high.
5. There are three ways to add a torrent to Joal:
    - Drag and drop torrents onto the dashboard
    - Click on "+" and select the torrent
    - Place the torrent in the `joal/data/torrents` folder
6. It is preferable to choose torrents with many seeds to ensure that sharing works properly.

## Start seeding

Just add some `.torrent` files to the `joal-conf/torrents` folder.  
There is no need to restart JOAL to add more torrents; add them to the folder and JOAL will recognize them after a few seconds.  
If the web UI is enabled, you can also drag and drop torrents into the Joal UI.

## Configuration file

### Application configuration

The application configuration belongs in `joal-conf/config.json`:

```json
{
  "minUploadRate": 30,
  "maxUploadRate": 160,
  "simultaneousSeed": 20,
  "client": "qbittorrent-3.3.16.client",
  "keepTorrentWithZeroLeechers": true,
  "uploadRatioTarget": -1.0
}
```

- `minUploadRate`: The minimum upload rate you want to fake (in kB/s) (**required**)
- `maxUploadRate`: The maximum upload rate you want to fake (in kB/s) (**required**)
- `simultaneousSeed`: How many torrents should be seeding at the same time (**required**)
- `client`: The name of the `.client` file to use from `joal-conf/clients/` (**required**)
- `keepTorrentWithZeroLeechers`: Should JOAL keep torrents with no leechers or seeders? If true, torrents with no peers will be seeded at 0kB/s. If false, torrents will be deleted when 0 peers are reached. (**required**)
- `uploadRatioTarget`: When JOAL has uploaded X times the size of the torrent **in a single session**, the torrent is removed. If -1.0, torrents are never removed.

## Supported browsers (for web-UI)

| Browser                               | Support                | Comment                                              |
| -------------------------------------  |:---------------------: | ---------------------------------------------------- |
| ![Google Chrome][browser-chrome]      | ![yes][support-yes]    |                                                      |
| ![Mozilla Firefox][browser-firefox]   | ![yes][support-yes]    |                                                      |
| ![Opera][browser-opera]               | ![yes][support-yes]    |                                                      |
| ![Opera mini][browser-opera-mini]     | ![no][support-no]      | Lack of `referrer-policy` & No support for WebSocket |
| ![Safari][browser-safari]             | ![no][support-no]      | Lack of `referrer-policy`                            |
| ![Edge][browser-edge]                 | ![no][support-no]      | Lack of `referrer-policy`                            |
| ![Internet Explorer][browser-ie]      | ![no][support-never]   | Not enough space to explain...                       |

Some non-supported browsers might work, but they may be unsafe due to the lack of support for `referrer-policy`.

## Community projects

These projects are maintained by their individual authors. For any questions, use the corresponding repository. I do not offer support nor take responsibility for these projects. But I want to say a special **thanks** to them for spending some time on this project.

- [Addon for Home Assistant](https://github.com/alexbelgium/hassio-addons/tree/master/joal) by [alexbelgium](https://github.com/alexbelgium)

- [Ansible role](https://github.com/slundi/ansible-joal) by [slundi](https://github.com/slundi)

# Thanks

This project uses a modified version of the awesome [mpetazzoni/ttorrent](https://github.com/mpetazzoni/ttorrent) library. Thanks to **mpetazzoni** for this.  
Also, this project has benefited from the help of several people – see [Thanks.md](THANKS.md).

## Supporters

[![Thanks for providing Jetbrain license](readme-assets/jetbrains.svg)](https://www.jetbrains.com/?from=joal)

[support-never]: readme-assets/warning.png
[support-no]: readme-assets/cross-mark.png
[support-yes]: readme-assets/check-mark.png
[joal-conf-folder]: readme-assets/joal-conf-folder.png
[browser-chrome]: readme-assets/browsers/chrome.png
[browser-firefox]: readme-assets/browsers/firefox.png
[browser-opera]: readme-assets/browsers/opera.png
[browser-opera-mini]: readme-assets/browsers/opera-mini.png
[browser-safari]: readme-assets/browsers/safari.png
[browser-ie]: readme-assets/browsers/ie.png
[browser-edge]: readme-assets/browsers/edge.png
[jetbrain-logo]: readme-assets/jetbrains.svg