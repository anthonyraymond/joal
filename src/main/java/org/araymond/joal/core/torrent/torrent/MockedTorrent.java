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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Created by raymo on 23/01/2017.
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@EqualsAndHashCode
@Getter
public class MockedTorrent extends Torrent {
    public static final Charset BYTE_ENCODING = StandardCharsets.ISO_8859_1;

    private final InfoHash torrentInfoHash;
    /**
     * Create a new torrent from meta-info binary data.
     * <p>
     * Parses the meta-info data (which should be B-encoded as described in the
     * BitTorrent specification) and create a Torrent object from it.
     *
     * @param torrent The meta-info byte data.
     * @param seeder  Whether we're the _initial_ seeder for this torrent.
     * @throws IOException When the info dictionary can't be read or
     *                     encoded and hashed back to create the torrent's SHA-1 hash.
     */
    private MockedTorrent(final byte[] torrent, final boolean seeder) throws IOException, NoSuchAlgorithmException {
        super(torrent, seeder);

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

    private MockedTorrent(byte[] data) throws IOException, NoSuchAlgorithmException {
        this(data, false);
    }

    public static MockedTorrent fromFile(final File torrentFile) throws IOException, NoSuchAlgorithmException {
        final byte[] data = FileUtils.readFileToByteArray(torrentFile);
        return new MockedTorrent(data);
    }

    public static MockedTorrent fromBytes(final byte[] bytes) throws IOException, NoSuchAlgorithmException {
        return new MockedTorrent(bytes);
    }
}
