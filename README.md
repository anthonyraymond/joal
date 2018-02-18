[![Build Status](https://travis-ci.org/anthonyraymond/joal.svg?branch=master)](https://travis-ci.org/anthonyraymond/joal)

# JOAL
This is the server application (with an **optional** webui), if you are interested in the desktop app look at [here](https://github.com/anthonyraymond/joal-desktop).

### Which client can JOAL emulate?

| Client        | Support                       | Comment        |  | Client        | Support                       | Comment        |
| ------------- |:-----------------------------:|----------------|--|---------------|:-----------------------------:|----------------|
| BitComet      | ![Numwant mess][support-never]| Will never be !|  | Transmission  | ![Yes][support-yes]           |                |
| BitTorrent    | ![Yes][support-yes]           |                |  | µTorrent      | ![Yes][support-yes]           |                |
| Deluge        | ![Yes][support-yes]           |                |  | Vuze Azureus  | ![Yes][support-yes]           |                |
| qBittorrent   | ![Yes][support-yes]           |                |  | Vuze Leap     | ![Yes][support-yes]           |                |
| rTorrent      | ![Yes][support-yes]           |                |  |

If your favorite client is not yet supported feel free to ask (except for BitComet).<br/>
Ask for it in GitHub issues or mail <a href="mailto:joal.contact@gmail.com">joal.contact@gmail.com</a>.

## Preview
![preview](readme-assets/webui-preview.png?raw=true)


## Getting started
Download the [latest tar.gz release](https://github.com/anthonyraymond/joal/releases/latest).
Put `config.json`, `clients`, and `torrents` folder into the location of your choice (this will be your configuration folder). For the rest of this README, it will be named `joal-conf`.

The folder should look like this:<br/>
![joal-conf][joal-conf-folder]

## How to run

```
java -Djava.net.preferIPv6Addresses=true -jar ./jack-of-all-trades-X.X.X.jar --joal-conf="PATH_TO_CONF"
```

- `-Djava.net.preferIPv6Addresses=true`: is for instruct the JVM to prefer ipv6, if you ISP does not support ipv6 don't prepend this parameter.
- `--joal-conf=PATH_TO_CONF` is a **required** argument: path to the joal-conf folder (ie: /home/anthony/joal-conf).

<br />
By default the web-ui is disabled, you can enable it with some more arguments:

- `--spring.main.web-environment=true`: to enable the web context.
- `--server.port=YOUR_PORT`: the port to be used for both HTTP and WebSocket connection.
- `--joal.ui.path.prefix="SECRET_OBFUSCATION_PATH"`: use your own complicated path here (this will be your first layer of security to keep joal secret). This is security though obscurity, but it is required in our case.  
- `--joal.ui.secret-token="SECRET_TOKEN"`: use your own secret token here (this is some kind of a password, choose a complicated one).

Once joal is started head to: `http://localhost:port/SECRET_OBFUSCATION_PATH/ui/` (obviously, replace `SECRET_OBFUSCATION_PATH`) by the value you had chosen
The `joal.ui.path.prefix` might seems useless but it's actually **crucial** to set it as complex as possible to prevent peoples to know that joal is running on your server.


## Start seeding
Just add some `.torrent` files to the `joal-conf/torrents` folder. There is no need to restart JOAL to add more torrents, add it to the folder and JOAL will be aware of after few seconds.

If WebUi is enabled you can also drag and drop torrents in the joal ui.

## Docker
Build it:
```
docker build -f Dockerfile -t araymond/joal .
```
If you want to build the **raspberry** docker image replace `Dockerfile` with `Dockerfile.arm`.

Then run it:
In next command you have to replace `PATH_TO_CONF`, `PORT`, `SECRET_OBFUSCATION_PATH` and `SECRET_TOKEN`.
```
docker run -d \
    -p PORT:PORT \
    -e _JAVA_OPTIONS='-Djava.net.preferIPv6Addresses=true' \
    -v PATH_TO_CONF:/data \
    --name="joal" araymond/joal \
    --joal-conf="/data" \
    --spring.main.web-environment=true \
    --server.port="PORT" \
    --joal.ui.path.prefix="SECRET_OBFUSCATION_PATH" \
    --joal.ui.secret-token="SECRET_TOKEN"
```
You can pass along all the CLI argument to customize JOAL.



## Configuration file
### Application configuration
The application configuration belongs in `joal-conf/config.json`.

```
{
  "minUploadRate" : 30,
  "maxUploadRate" : 160,
  "simultaneousSeed" : 20,
  "client" : "qbittorrent-3.3.16.client",
  "keepTorrentWithZeroLeechers" : true
}
```
- `minUploadRate` : The minimum uploadRate you want to fake (in kB/s) (**required**)
- `maxUploadRate` : The maximum uploadRate you want to fake (in kB/s) (**required**)
- `simultaneousSeed` : How many torrents should be seeding at the same time (**required**)
- `client` : The name of the .client file to use in `joal-conf/clients/` (**required**)
- `keepTorrentWithZeroLeechers`: should JOAL keep torrent with no leechers or seeders. If yes, torrent with no peers will be seed at 0kB/s. If false torrents will be deleted on 0 peers reached. (**required**)



## Supported browser (for web-ui)
| Client                              | Support                 | Comment                                              |
| ----------------------------------- |:-----------------------:|------------------------------------------------------|
| ![Google Chrome][browser-chrome]    | ![yes][support-yes]     |                                                      |
| ![Mozilla Firefox][browser-firefox] | ![yes][support-yes]     |                                                      |
| ![Opera][browser-opera]             | ![yes][support-yes]     |                                                      |
| ![Opera mini][browser-opera-mini]   | ![no][support-no]       | Lack of `referrer-policy` & No support for WebSocket |
| ![Safari][browser-safari]           | ![no][support-danger]   | Lack of `referrer-policy`                            |
| ![Edge][browser-edge]               | ![no][support-danger]   | Lack of `referrer-policy`                            |
| ![Internet explorer][browser-ie]    | ![no][support-danger]   | Not enough space to explain...                       |

Some non-supported browser might works, but they may be unsafe due to the lack of support for `referrer-policy`.

# Thanks:
This project use a modified version of the awesome [mpetazzoni/ttorrent](http://mpetazzoni.github.com/ttorrent/) library. Thanks to **mpetazzoni** for this.
Also this project has benefited from the help of several peoples, see [Thanks.md](THANKS.md)


[support-danger]:readme-assets/warning.png
[support-no]:readme-assets/cross-mark.png
[support-yes]:readme-assets/check-mark.png
[joal-conf-folder]:readme-assets/joal-conf-folder.png
[browser-chrome]:readme-assets/browsers/chrome.png
[browser-firefox]:readme-assets/browsers/firefox.png
[browser-opera]:readme-assets/browsers/opera.png
[browser-opera-mini]:readme-assets/browsers/opera-mini.png
[browser-safari]:readme-assets/browsers/safari.png
[browser-ie]:readme-assets/browsers/ie.png
[browser-edge]:readme-assets/browsers/edge.png
