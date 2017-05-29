Un guide en français est disponible [ici](http://h5ckfun.info/joal-le-nouveau-ratiomaster/)

# Prepare configuration folder
You first need to get the configuration folder. Replace `<MY_CONFIG_FOLDER_PATH>` by whatever path you want.
```
git clone git@github.com:anthonyraymond/joal.git
cd joal
cp -R resources <MY_CONFIG_FOLDER_PATH>
```
Now add some of your private tracker's `.torrent` file to `<MY_CONFIG_FOLDER_PATH>/torrents/`. But **be aware** that only torrent with at least 1 leecher will actually be used by *joal*, to keep your account from being banned.


# Install and run
### With Docker

**For ARM distribution only (raspberry)**
```
docker build -f Dockerfile.arm -t araymond/joal .
docker run -d -v <PATH_TO_CONFIG_DIR>:/data -p 49152-65534:49152 --name="joal" araymond/joal
```

**For any other distribution**
```
docker build -t araymond/joal .
docker run -d -v <PATH_TO_CONFIG_DIR>:/data -p 49152-65534:49152 --name="joal" araymond/joal
```

### Without Docker
You need to have **Java 8** installed.

We first need to download the latest version of the .jar. Once again replace  `<MY_CONFIG_FOLDER_PATH>` by your chosen path and execute this script
```
cd <MY_CONFIG_FOLDER_PATH> \
&& GITHUB_REPO="https://github.com/anthonyraymond/joal" \
&& LATEST=$(curl -sSI $GITHUB_REPO"/releases/latest" | perl -n -e '/^Location: .*?tag\/(.*?)\r*$/ && print "$1\n"') \
&& curl -f -L $GITHUB_REPO"/releases/download/"$LATEST"/"$LATEST".jar" > ./jack-of-all-trades.jar
```

Then you can run the application with the following command *(replace X.X.X with your own version)*
```
java -jar jack-of-all-trades-X.X.X.jar <MY_CONFIG_FOLDER_PATH>
```

# Understanding configuration (optional)
### Torrent files
- All torrent file in `<MY_CONFIG_FOLDER_PATH>/torrents/` will be available for sharing.
- One random torrent file from `<MY_CONFIG_FOLDER_PATH>/torrents/` is chosen randomly for each seed session.
- All torrent file added/removed/modified in `<MY_CONFIG_FOLDER_PATH>/torrents/` while the client is running will be automatically hot loaded, there is no need to restart.
- **If the torrent currently seeding reach 0 peers, the file will be moved to `<MY_CONFIG_FOLDER_PATH>/torrents/archived` folder.**


### Application configuration
The application configuration belongs in `<MY_CONFIG_FOLDER_PATH>/config.json`.

```
{
  "minUploadRate": 180,
  "maxUploadRate": 190,
  "seedFor": 840,
  "waitBetweenSeed": 600,
  "client": "azureus-5.7.4.0.client"
}
```
- `minUploadRate` : The minimum uploadRate you want to fake (in kB/s) (**required**)
- `maxUploadRate` : The maximum uploadRate you want to fake (in kB/s) (**required**)
- `seedFor` : How long the client should seed for in a row (seeding session in minutes) (**required**)
- `waitBetweenSeed` : How long the client should wait before two seeding session (in minutes) (**required**)
- `client` : The name of the .client file to use in `<MY_CONFIG_FOLDER_PATH>/clients/` (**required**)

### Create your own torrent clients
To learn more about .client, head to the [project's wiki][project-wiki].

## ROADMAP to v2
- https://github.com/anthonyraymond/joal/issues/4

# Thanks:
This project use a modified version of the awesome [mpetazzoni/ttorrent] library. Thanks to **mpetazzoni** for this.

[project-wiki]: https://github.com/anthonyraymond/joal/wiki
[mpetazzoni/ttorrent]: http://mpetazzoni.github.com/ttorrent/

[![Analytics](https://ga-beacon.appspot.com/UA-97530761-1/joal/readme?pixel)](https://github.com/igrigorik/ga-beacon)
