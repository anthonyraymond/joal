package org.araymond.joal.core.ttorent.client;

import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

/**
 * Created by raymo on 23/01/2017.
 */
public final class MockedTorrent extends Torrent {

    private final int pieceLength;
    private final ByteBuffer piecesHashes;

    private long uploaded;
    private final long downloaded;
    private final long left;

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
    private MockedTorrent(final byte[] torrent, final boolean seeder) throws IOException, NoSuchAlgorithmException {
        super(torrent, seeder);

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

        this.uploaded = 0;
        this.downloaded = 0;
        // Left is fixed mocked value, since torrent is supposed to be completely downloaded already.
        this.left = 0;
    }

    public static MockedTorrent fromFile(final File torrent) throws IOException, NoSuchAlgorithmException {
        final byte[] data = FileUtils.readFileToByteArray(torrent);
        return new MockedTorrent(data, true);
    }

    /**
     * Get the number of bytes uploaded for this torrent.
     */
    public long getUploaded() {
        return this.uploaded;
    }

    public void addUploaded(final long bytes) {
        this.uploaded += bytes;
    }

    /**
     * Get the number of bytes downloaded for this torrent.
     *
     * <p>
     * <b>Note:</b> this could be more than the torrent's length, and should
     * not be used to determine a completion percentage.
     * </p>
     */
    public long getDownloaded() {
        return this.downloaded;
    }

    /**
     * Get the number of bytes left to download for this torrent.
     */
    public long getLeft() {
        return this.left;
    }

}
