package org.araymond.joal.core.ttorent.client;

import com.google.common.base.Objects;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

/**
 * Created by raymo on 23/01/2017.
 */
public class MockedTorrent extends Torrent {

    private final int pieceLength;
    private final ByteBuffer piecesHashes;
    private final Path path;

    /**
     * Create a new torrent from meta-info binary data.
     * <p>
     * Parses the meta-info data (which should be B-encoded as described in the
     * BitTorrent specification) and create a Torrent object from it.
     *
     * @param torrent The meta-info byte data.
     * @param seeder  Whether we'll be seeding for this torrent or not.
     * @throws IOException When the info dictionary can't be read or
     *                     encoded and hashed back to create the torrent's SHA-1 hash.
     */
    private MockedTorrent(final byte[] torrent, final boolean seeder, final Path path) throws IOException, NoSuchAlgorithmException {
        super(torrent, seeder);
        this.path = path;

        try {
            this.pieceLength = this.decoded_info.get("piece length").getInt();
            this.piecesHashes = ByteBuffer.wrap(this.decoded_info.get("pieces").getBytes());

            if (this.piecesHashes.capacity() / Torrent.PIECE_HASH_SIZE *
                    (long)this.pieceLength < this.getSize()) {
                throw new IllegalArgumentException("Torrent size does not " +
                        "match the number of pieces and the piece size!");
            }
        } catch (final InvalidBEncodingException ibee) {
            throw new IllegalArgumentException("Error reading torrent meta-info fields!");
        }

    }

    public static MockedTorrent fromFile(final File torrent) throws IOException, NoSuchAlgorithmException {
        final byte[] data = FileUtils.readFileToByteArray(torrent);
        return new MockedTorrent(data, true, torrent.toPath());
    }

    @Override
    public boolean equals(final Object o) {
        // TODO : consider a better way to handle equals and hashcode
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MockedTorrent that = (MockedTorrent) o;
        return Objects.equal(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
