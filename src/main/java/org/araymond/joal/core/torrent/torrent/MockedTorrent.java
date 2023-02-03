package org.araymond.joal.core.torrent.torrent;

import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

/**
 * Created by raymo on 23/01/2017.
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@EqualsAndHashCode(callSuper = false)
@Getter
public class MockedTorrent extends Torrent {
    public static final Charset BYTE_ENCODING = Charset.forName(Torrent.BYTE_ENCODING);

    private final InfoHash torrentInfoHash;

    /**
     * Create a new torrent from meta-info binary data.
     * <p>
     * Parses the meta-info data (which should be B-encoded as described in the
     * BitTorrent specification) and create a Torrent object from it.
     *
     * @param torrent The meta-info byte data.
     * @throws IOException When the info dictionary can't be read or
     *                     encoded and hashed back to create the torrent's SHA-1 hash.
     */
    private MockedTorrent(final byte[] torrent) throws IOException, NoSuchAlgorithmException {
        super(torrent, false);

        try {
            // Torrent validity tests
            final int pieceLength = this.decoded_info.get("piece length").getInt();
            final ByteBuffer piecesHashes = ByteBuffer.wrap(this.decoded_info.get("pieces").getBytes());

            if (piecesHashes.capacity() / Torrent.PIECE_HASH_SIZE * (long) pieceLength < this.getSize()) {
                throw new IllegalArgumentException("Torrent size does not match the number of pieces and the piece size!");
            }
        } catch (final InvalidBEncodingException ex) {
            throw new IllegalArgumentException("Error reading torrent meta-info fields!", ex);
        }

        this.torrentInfoHash = new InfoHash(this.getInfoHash());
    }

    public static MockedTorrent fromFile(final File torrentFile) throws IOException, NoSuchAlgorithmException {
        final byte[] data = FileUtils.readFileToByteArray(torrentFile);
        return new MockedTorrent(data);
    }

    public static MockedTorrent fromBytes(final byte[] bytes) throws IOException, NoSuchAlgorithmException {
        return new MockedTorrent(bytes);
    }
}
