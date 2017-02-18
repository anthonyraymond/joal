# Disclamer
This soft is provided as is, without any warrantly.

This project use a modified version of the awesome [mpetazzoni/ttorrent] library. Thanks to **mpetazzoni** for this.

# How to use
### 1. Configuration
```
git clone git@github.com:anthonyraymond/joal.git
cd joal
cp -R resources <MY_CONFIG_FOLDER_PATH>
```
We are almost ready to go, we still need to add some of your private tracker's `.torrent` file to `<MY_CONFIG_FOLDER_PATH>/torrents/`

```
cp *.torrent <MY_CONFIG_FOLDER_PATH>/torrents/
```
We are now ready to press the red button and fire up the ratio faker.

### 2. Building and running

##### With Docker
At the moment only an ARM based docker file is available.
```
docker build -f Dockerfile.arm -t araymond/joal .
docker run -d -v <PATH_TO_CONFIG_DIR>:/data -p 49152-65534:49152 --name="joal" araymond/joal
```

##### Without Docker
```
cd joal
mvn clean package
java -jar target/jack-of-all-trades-1.0-SNAPSHOT.jar /<MY_CONFIG_FOLDER_PATH>
```

# How it works
#### Application configuration
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


#### Torrent files
- All torrent file in `<MY_CONFIG_FOLDER_PATH>/torrents/` will be available for sharing.
- The torrent file to share is randomly selected between all files on every seed session (seed session duration is managed by the configuration file).
- All torrent file added/removed/modified in `<MY_CONFIG_FOLDER_PATH>/torrents/` while the client is running will be automatically hot loaded, there is no need to restart.
- If the torrent currently seeding reach 0 peers, the .torrent file associated is removed from the disk.

#### Emulated client file
Event if the soft works out of the box, you can add your own torrent clients.

###### .client file format
The below file correspond to azureus-3.0.5.0.client
```json
{
    "peerIdInfo": {
        "prefix": "-AZ3050-",
        "type": "alphanumeric",
        "upperCase": false,
        "lowerCase": false
    },
    "keyInfo": {
        "length": 8,
        "type": "alphanumeric",
        "upperCase": false,
        "lowerCase": false
    },
    "query": "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3",
    "numwant": 100,
    "requestHeaders": [
      { "name": "User-Agent", "value":"Azureus 3.0.5.0;{os};{java}" },
      { "name": "Connection", "value": "close" },
      { "name": "Accept-Encoding", "value":"gzip" },
      { "name": "Host", "value":"{host}" },
      { "name": "Accept", "value":"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" },
      { "name": "Content-type", "value":"application/x-www-form-urlencoded" }
    ]
}
```
- peerIdInfo (**required**)
  - prefix    : The BitTorrentClient prefix (**required**)
  - type      : The type of the peer_id_suffix (**required**, supported types: alphabetic, alphanumeric, numeric, random, printable, hexadecimal)
  - upperCase : Is the suffix uppercased? (optional, default=false)
  - lowerCase : Is the suffix lowercased? (optional, default=false)

- keyInfo (**required only if query contains {key}**)
  - length    : Length of the key (**required**)
  - type      : The type of the peer_id_suffix (**required**, supported types: alphabetic, alphanumeric, numeric, random, printable, hexadecimal)
  - upperCase : Is the suffix uppercased? (optional, default=false)
  - lowerCase : Is the suffix lowercased? (optional, default=false)

- query (**required**) : The BitTorrent client HTTP query string with **variables**

- numwant (optional, default 50) : The BitTorrent numwant

- requestHeaders (optional)

###### Query variables:
- `{peerid}`     : BitTorrent client peerId (value from **peerIdInfo**)
- `{key}`        : The key for the current session (value from **keyInfo**, optional if there is no key param in query string)
- `{numwant}`    : BitTorrent client numwant (default is 50)  
- `{infohash}`   : Torrent file info_hash (auto-generated)
- `{uploaded}`   : Total uploaded during this session (auto-generated)
- `{downloaded}` : Total downloaded during this session (auto-generated)
- `{left}`       : Remaining to download for this torrent (auto-generated, and hardcoded to 0)
- `{port}`       : Port you are listening on (auto-generated)
- `{ip}`         : Local ip address (auto-generated)
- `{event}`      : Event to be send to the tracker (auto-generated)

###### Header variables:
- `{host}`       : The remote host (auto-generated)
- `{os}`         : The current os (auto-generated)
- `{java}`       : The current running version of java (auto-generated)


## ROADMAP
- [x] Add application setting with setting file
- [x] App setting : min_upload_rate
- [x] App setting : max_upload_rate
- [x] Externalise client definition to .clients file
- [x] Add client header to HttpRequest
- [x] When a torrent reach 0 peers, try with another torrent instead of stopping.
- [x] When a torrent reach 0 peers, remove the torrent file from the directory.
- [x] Add a file watcher to monitor torrent folder (hot loading .torrent files instead of restarting)
- [x] Implement appear as contactable to peers.
- [ ] Add a config to define if torrent should be deleted or skipped if 0 peers were leeching.
- [ ] Add test.
- [ ] Refactoring.


[mpetazzoni/ttorrent]: http://mpetazzoni.github.com/ttorrent/
