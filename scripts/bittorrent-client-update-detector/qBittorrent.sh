#!/bin/bash

. ./libtorrent_funcs.sh

# clean tempSource folder
qBittorrentTempFolder="./tempSource/qBittorrent"
rm -rf $qBittorrentTempFolder
mkdir -p $qBittorrentTempFolder


if [ -z ${1+x} ]; then
  # Download latest release
  tarballUrl=$(curl -s https://api.github.com/repos/qbittorrent/qBittorrent/tags \
    | grep "tarball_url" \
    | head -1 \
    | cut -d : -f 2,3 \
    | cut -d , -f 1 \
    | tr -d \")
else
  # download the release from the "tarball_url" given in parameter (see https://api.github.com/repos/qbittorrent/qBittorrent/tags)
  tarballUrl=$1
fi
curl -L $tarballUrl --output $qBittorrentTempFolder/qBittorrent.tar.gz

# uncompress the archive
tar -xzf $qBittorrentTempFolder/qBittorrent.tar.gz -C $qBittorrentTempFolder/ --strip 1

# seach for qBittorent versions
VER_MAJOR=$(grep "#define QBT_VERSION_MAJOR " $qBittorrentTempFolder/src/base/version.h.in | cut -d ' ' -f 3 | tr -d '[:space:]')
VER_MINOR=$(grep "#define QBT_VERSION_MINOR " $qBittorrentTempFolder/src/base/version.h.in | cut -d ' ' -f 3 | tr -d '[:space:]')
VER_BUGFIX=$(grep "#define QBT_VERSION_BUGFIX " $qBittorrentTempFolder/src/base/version.h.in | cut -d ' ' -f 3 | tr -d '[:space:]')
VER_BUILD=$(grep "#define QBT_VERSION_BUILD " $qBittorrentTempFolder/src/base/version.h.in | cut -d ' ' -f 3 | tr -d '[:space:]')
VER_STATUS=$(grep "#define QBT_VERSION_STATUS " $qBittorrentTempFolder/src/base/version.h.in | cut -d ' ' -f 3 | cut -d '"' -f 1 | tr -d '[:space:]')

PROJECT_VERSION="${VER_MAJOR}.${VER_MINOR}.${VER_BUGFIX}"
if [ $VER_BUILD -ne '0' ]; then
  PROJECT_VERSION="${PROJECT_VERSION}.${VER_BUILD}"
fi
PROJECT_VERSION="${PROJECT_VERSION}${VER_STATUS}"

QBT_VERSION_MAJOR=${VER_MAJOR}
QBT_VERSION_MINOR=${VER_MINOR}
QBT_VERSION_BUGFIX=${VER_BUGFIX}
QBT_VERSION_BUILD=${VER_BUILD}
QBT_VERSION="v${PROJECT_VERSION}"
QBT_VERSION_2="${PROJECT_VERSION}"

# extract user agent
non_expanded_user_agent=$(grep "USER_AGENT\[\] =" $qBittorrentTempFolder/src/base/bittorrent/session.cpp | cut -d '=' -f 2 | tr -d '[:space:]' | tr -d '[";]' | sed -e 's/QBT_VERSION/$QBT_VERSION/g')
user_agent=$(eval echo "$non_expanded_user_agent")
echo "User-Agent is: $user_agent"


# extract beginning of peer_id
bt_peer_id_small_name=$(grep "PEER_ID\[\] =" $qBittorrentTempFolder/src/base/bittorrent/session.cpp | cut -d '=' -f 2 | tr -d '[:space:]' | tr -d '[";]')

if [ $(grep -c "lt::generate_fingerprint(PEER_ID, QBT_VERSION_MAJOR, QBT_VERSION_MINOR, QBT_VERSION_BUGFIX, QBT_VERSION_BUILD);" $qBittorrentTempFolder/src/base/bittorrent/session.cpp) -lt 1 ]; then
  echo "WHHHHHOOOOPS, the peerid prefix generator might have changed."
  exit 1
fi

peer_id_prefix=$(libtorrent__compute_peer_id_prefix "$bt_peer_id_small_name" "$QBT_VERSION_MAJOR" "$QBT_VERSION_MINOR" "$QBT_VERSION_BUGFIX" "$QBT_VERSION_BUILD")
echo "Peer_id prefix is: $peer_id_prefix"

echo "key : qBittorent is using libtorrent => $(libtorrent_get_key_format)"


# clean tempSource folder
rm -rf $qBittorrentTempFolder
