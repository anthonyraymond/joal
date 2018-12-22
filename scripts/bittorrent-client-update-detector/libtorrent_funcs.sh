
version_to_char() {
  if [ $1 -ge 0 ] && [ $1 -lt 10 ]; then
    echo $1
  elif [ $1 -ge 10 ]; then
     #return char('A' + (v - 10));
     echo 0x$(( $(printf "%x" "'A'") + ($1 - 10))) | xxd -r
  fi
}

libtorrent__compute_peer_id_prefix() {
  prefix='-'
  prefix=$prefix$(echo $1 | head -c 2)
  prefix=$prefix$(version_to_char $2)
  prefix=$prefix$(version_to_char $3)
  prefix=$prefix$(version_to_char $4)
  prefix=$prefix$(version_to_char $5)
  prefix=$prefix'-'

  echo "$prefix"
}

libtorrent_get_key_format() {
  # fn de generation de la clé: https://github.com/arvidn/libtorrent/blob/master/src/torrent.cpp std::uint32_t torrent::tracker_key() const
  # Key format (uppercase) https://github.com/arvidn/libtorrent/blob/master//src/http_tracker_connection.cpp "&key=%08X" (capital X means uppercased hexa, %08 means length of 8 and left-padded with 0)
  echo "Libtorrent generate keys matching regex pattern [0-F]{8} (uppercased)"
}