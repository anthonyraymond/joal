#!/bin/bash

transmissionTempFolder="./tempSource/transmission"
rm -rf $transmissionTempFolder
mkdir -p $transmissionTempFolder


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


if [ $(grep -c '(e, CURLOPT_USERAGENT, TR_NAME "/" SHORT_VERSION_STRING);' $transmissionTempFolder/libtransmission/web.c) -lt 1 ]; then
  echo "WHHHHHOOOOPS, the user agent generator might have changed."
  exit 1
fi
userAgentPattern=$(grep "CURLOPT_USERAGENT" $transmissionTempFolder/libtransmission/web.c)
userAgentPattern=${userAgentPattern##*,} # get text after last comma
userAgentPattern=$(echo $userAgentPattern | sed 's# "/" #/#g' | sed 's/);//g') # remove double quotes and spaces separators

TR_NAME=$(grep "TR_NAME" $transmissionTempFolder/libtransmission/session.h | cut -d'"' -f 2) # Get the value between quotes

if [ $(grep -c '#define SHORT_VERSION_STRING      "${TR_USER_AGENT_PREFIX}"' $transmissionTempFolder/libtransmission/version.h.in) -lt 1 ]; then
  echo "WHHHHHOOOOPS, the user agent SHORT_VERSION_STRING might have changed."
  exit 1
fi
SHORT_VERSION_STRING=$(grep "set(TR_USER_AGENT_PREFIX" $transmissionTempFolder/CMakeLists.txt | cut -d'"' -f 2) # Get the value between quotes


userAgent=$(echo $userAgentPattern | sed "s/TR_NAME/${TR_NAME}/g" |sed "s/SHORT_VERSION_STRING/${SHORT_VERSION_STRING}/g")
echo "User-Agent is: $userAgent"


peer_id_prefix_expr=$(grep "peer_id_prefix=" $transmissionTempFolder/update-version-h.sh | sed 's/peer_id_prefix=//g' | sed 's|configure.ac|$transmissionTempFolder/configure.ac|')

peer_id_prefix=$(eval echo "$peer_id_prefix_expr")
echo "Peer_id prefix is: $peer_id_prefix"



echo "key : An int between 1 and 2147483647 (inclusive) which is converted to hex (without leading zero)"


# clean tempSource folder
rm -rf $transmissionTempFolder

# fn du user-agent : https://github.com/transmission/transmission/blob/master/libtransmission/web.c static CURL* createEasy(tr_session* s, struct tr_web* web, struct tr_web_task* task) => curl_easy_setopt(e, CURLOPT_USERAGENT, TR_NAME "/" SHORT_VERSION_STRING);

# fn de generation des peerid : https://github.com/transmission/transmission/blob/eb5d1a79cbe1b9bc5b22fdcc598694ecd4d02f43/libtransmission/session.c > void tr_peerIdInit(uint8_t* buf)

# fn de generation des key : https://github.com/transmission/transmission/blob/a86266d3c29f6a5b4103d9c3d60e10165d410226/libtransmission/announcer.c > void tr_announcerInit(tr_session* session)

# fn de creation des url d'announce : https://github.com/transmission/transmission/blob/c11f2870fd18ff781ca06ce84b6d43541f3293dd/libtransmission/announcer-http.c static char* announce_url_new(tr_session const* session, tr_announce_request const* req)