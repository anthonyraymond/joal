#!/bin/bash

downloadAndExtract() {
  if [ -z ${1+x} ]; then
    # Download latest release
    tarballUrl=$(curl -s https://api.github.com/repos/transmission/transmission/releases/latest \
      | grep "tarball_url" \
      | head -1 \
      | cut -d : -f 2,3 \
      | cut -d , -f 1 \
      | tr -d \")
  else
    # download the release from the "tarball_url" given in parameter (see https://api.github.com/repos/transmission/transmission/releases)
    tarballUrl=$1
  fi
  curl -L $tarballUrl --output $transmissionTempFolder/transmission.tar.gz

  # uncompress the archive
  tar -xzf $transmissionTempFolder/transmission.tar.gz -C $transmissionTempFolder/ --strip 1
}

getUserAgent () {
  # double check that the userAgent generation has not changed
  if [ $(grep -c 'return TR_NAME "/" SHORT_VERSION_STRING;' $transmissionTempFolder/libtransmission/session.cc) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the user agent generator might have changed."
    exit 1
  fi


  # userAgent is a concatenation of TR_NAME + "/" + SHORT_VERSION_STRING
  local userAgentPattern="TR_NAME/SHORT_VERSION_STRING"

  # get TR_NAME value
  local TR_NAME=$(grep "TR_NAME" $transmissionTempFolder/libtransmission/session.h | cut -d'"' -f 2) # Get the value between quotes


  # get SHORT_VERSION_STRING value
  # SHORT_VERSION_STRING = "${TR_USER_AGENT_PREFIX}"; TR_USER_AGENT_PREFIX = "${TR_SEMVER}"; TR_SEMVER="${TR_VERSION_MAJOR}.${TR_VERSION_MINOR}.${TR_VERSION_PATCH}"
  if [ $(grep -cF '#define SHORT_VERSION_STRING      "${TR_USER_AGENT_PREFIX}"' $transmissionTempFolder/libtransmission/version.h.in) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the user agent SHORT_VERSION_STRING might have changed."
    exit 1
  fi
  if [ $(grep -cF 'set(TR_USER_AGENT_PREFIX "${TR_SEMVER}")' $transmissionTempFolder/CMakeLists.txt) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the user agent TR_USER_AGENT_PREFIX variable might have changed."
    exit 1
  fi
  if [ $(grep -cF 'set(TR_SEMVER "${TR_VERSION_MAJOR}.${TR_VERSION_MINOR}.${TR_VERSION_PATCH}")' $transmissionTempFolder/CMakeLists.txt) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the user agent TR_SEMVER variable might have changed."
    exit 1
  fi

  local TR_VERSION_MAJOR=$(grep "set(TR_VERSION_MAJOR" $transmissionTempFolder/CMakeLists.txt | cut -d'"' -f 2) # Get the value between quotes
  local TR_VERSION_MINOR=$(grep "set(TR_VERSION_MINOR" $transmissionTempFolder/CMakeLists.txt | cut -d'"' -f 2) # Get the value between quotes
  local TR_VERSION_PATCH=$(grep "set(TR_VERSION_PATCH" $transmissionTempFolder/CMakeLists.txt | cut -d'"' -f 2) # Get the value between quotes
  
  local TR_SEMVER="${TR_VERSION_MAJOR}.${TR_VERSION_MINOR}.${TR_VERSION_PATCH}"
  local TR_USER_AGENT_PREFIX="${TR_SEMVER}"
  local SHORT_VERSION_STRING="${TR_USER_AGENT_PREFIX}"

  local userAgent=$(echo $userAgentPattern | sed "s/TR_NAME/${TR_NAME}/g" |sed "s/SHORT_VERSION_STRING/${SHORT_VERSION_STRING}/g")
  echo $userAgent
}

getPeerIdPrefix () {
  # double check that the peer_id_prefix generation has not changed
  if [ $(grep -cF 'peer_id_prefix="-TR${BASE62[$(( 10#$major_version ))]}${BASE62[$(( 10#$minor_version ))]}${BASE62[$(( 10#$patch_version ))]}"' $transmissionTempFolder/update-version-h.sh) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the peer_id_prefix generator might have changed."
    exit 1
  fi
  if [ $(grep -cF '#define PEERID_PREFIX             "${peer_id_prefix}"' $transmissionTempFolder/update-version-h.sh) -lt 1 ]; then
    echo "WHHHHHOOOOPS, the peer_id_prefix value attribution might have changed."
    exit 1
  fi

  local major_version=$(grep 'set[(]TR_VERSION_MAJOR' $transmissionTempFolder/CMakeLists.txt | cut -d \" -f 2)
  local minor_version=$(grep 'set[(]TR_VERSION_MINOR' $transmissionTempFolder/CMakeLists.txt | cut -d \" -f 2)
  local patch_version=$(grep 'set[(]TR_VERSION_PATCH' $transmissionTempFolder/CMakeLists.txt | cut -d \" -f 2)

  local BASE62=($(echo {0..9} {A..A} {a..z}))
  local peer_id_prefix=$"-TR${BASE62[$(( 10#$major_version ))]}${BASE62[$(( 10#$minor_version ))]}${BASE62[$(( 10#$patch_version ))]}"
  peer_id_prefix="${peer_id_prefix}0-"

  echo $peer_id_prefix
}


transmissionTempFolder="./tempSource/transmission"
rm -rf $transmissionTempFolder
mkdir -p $transmissionTempFolder

downloadAndExtract

userAgent="$(getUserAgent)"
echo "User-Agent is: $userAgent"

peerIdPrefix="$(getPeerIdPrefix)"
echo "Peer_id prefix is: $peerIdPrefix"

# fn de generation des key : https://github.com/transmission/transmission/blob/9d2507c7e32d60d91cf91dc6ef0147f568d44111/libtransmission/torrent.h > constexpr auto announce_key() const noexcept
echo "key : An int between 1 and 4294967295 (inclusive) which is converted to hex (without leading zero)"


# clean tempSource folder
rm -rf $transmissionTempFolder
