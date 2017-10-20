[![Build Status](https://travis-ci.org/anthonyraymond/joal.svg?branch=master)](https://travis-ci.org/anthonyraymond/joal)

# JOAL
This is the server application (with an **optional** webui), if you are interested in the desktop app look at [here](https://github.com/anthonyraymond/joal-desktop).

### Which client can JOAL emulate?

| Client        | Support                       | Comment        |
| ------------- |:-----------------------------:|----------------|
| BitComet      | ![FUCK NO ! numwant management is a mess !][support-fuck_no] | Will never be !|
| BitTorrent    | ![Yes][support-yes]           |                |
| Deluge        | ![Yes][support-yes]           |                |
| qBittorrent   | ![Yes][support-yes]           |                |
| rTorrent      | ![Yes][support-yes]           |                |
| Transmission  | ![Yes][support-yes]           |                |
| µTorrent      | ![Yes][support-yes]           |                |
| Vuze Azureus  | ![Yes][support-yes]           |                |
| Vuze Leap     | ![Yes][support-yes]           |                |

If your favorite client is not yet supported feel free to ask (except for BitComet).<br/>
Ask for it in GitHub issues or mail <a href="mailto:joal.contact@gmail.com">joal.contact@gmail.com</a>.


[support-fuck_no]:readme-assets/warning.png
[support-no]:readme-assets/cross-mark.png
[support-yes]:readme-assets/check-mark.png

## Getting started
Download the latest `tar.gz` release.
Put `config.json`, `clients`, and `torrents` folder into the location of your choice (this will be your configuration folder). For the rest of this README, it will be named `joal-conf`.

## How to run

```
java -Djava.net.preferIPv6Addresses=true -jar .\jack-of-all-trades-X.X.X.jar --joal-conf="PATH_TO_CONF" 
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

## Docker
Build it:
```
docker build -f Dockerfile -t araymond/joal .
```
If you want to build the **raspberry** docker image replace `Dockerfile` with `Dockerfile.arm`.

Then run it:
In next command you have to replace `PORT`, `PATH_TO_CONF`, `SECRET_OBFUSCATION_PATH` and `SECRET_TOKEN`.
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
- `keepTorrentWithZeroLeechers`: should JOAL keep torrent with no leechers or seeders. If yes, torrent with no peers will be seed at 0kB/s. If false torrents will be deleted on 0 peers reached.

# Thanks:
This project use a modified version of the awesome [mpetazzoni/ttorrent](http://mpetazzoni.github.com/ttorrent/) library. Thanks to **mpetazzoni** for this.
Also this project has benefited from the help of several peoples, see [Thanks.md](THANKS.md)

[![Analytics](https://ga-beacon.appspot.com/UA-97530761-1/joal/readme?pixel)](https://github.com/igrigorik/ga-beacon)
