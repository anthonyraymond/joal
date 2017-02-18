# Disclamer
This soft is provided as is, without any warrantly.

This project use http://mpetazzoni.github.com/ttorrent/ library. Thanks to mpetazzoni for this.

### How to use
1. Copy the `resources` on your disk.
2. Add some `.torrent` file to your `resources/torrents/` new folder.
3. Deploy with docker.

While the client is running all .torrent files added to the folder will be hot loaded, there is no need to restart the client.

#### Docker support
##### Build it
```
docker build -f Dockerfile.arm -t araymond/joal .
```

##### Run it
```
docker run -d -v <PATH_TO_CONFIG_DIR>:/data -p 49152-65534:49152 --name="joal" araymond/joal
```
Where `<PATH_TO_CONFIG_DIR>` is the resources folder path where you store:
- `config.json` : the configuration file.
- `clients/` : folder with all the clients files.
- `torrents/` : folder with all the .torrent files to seed.

### Emulated client file
Event if the soft works out of the box, you can add your own torrent clients.
A client file is required to start seeding, here a some instruction on what you can use to customise your client file.

#### .client file
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

#### Query variables:
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

#### Header variables:
- `{host}`       : The remote host (auto-generated)
- `{os}`         : The current os (auto-generated)
- `{java}`       : The current running version of java (auto-generated)


## ROADMAP
- [x] Add application setting with setting file
- [x] App setting : min_seed_time_per_day
- [x] App setting : max_seed_time_per_day
- [x] App setting : min_upload_rate
- [x] App setting : max_upload_rate
- [x] Externalise client definition to .clients file
- [x] Add client header to HttpRequest
- [x] If torrent command line argument is a folder, seed a random torrent from it.
- [x] When a torrent reach 0 peers, try with another torrent instead of stopping.
- [x] When a torrent reach 0 peers, remove the torrent file from the directory.
- [x] Add a file watcher to monitor torrent folder (hot loading .torrent files instead of restarting)
- [x] Implement appear as contactable to peers.
- [ ] Add a config to define if torrent should be deleted or skipped if 0 peers were leeching.
- [ ] Add test.
- [ ] Refactoring.
