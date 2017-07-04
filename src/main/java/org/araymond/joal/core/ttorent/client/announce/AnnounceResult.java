package org.araymond.joal.core.ttorent.client.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.time.LocalDateTime;

/**
 * Created by raymo on 03/07/2017.
 */
public abstract class AnnounceResult {
    enum AnnounceResultType {
        SUCCESS, ERROR;
    }

    private final LocalDateTime dateTime;
    private final AnnounceResultType type;

    protected AnnounceResult(final AnnounceResultType type) {
        Preconditions.checkNotNull(type, "AnnounceResultType must not be null");
        this.type = type;
        this.dateTime = LocalDateTime.now();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AnnounceResultType getType() {
        return type;
    }

    public static class SuccessAnnounceResult extends AnnounceResult {
        SuccessAnnounceResult() {
            super(AnnounceResultType.SUCCESS);
        }
    }


    public static class ErrorAnnounceResult extends AnnounceResult {

        private final String errMessage;

        ErrorAnnounceResult(final String errMessage) {
            super(AnnounceResultType.ERROR);
            Preconditions.checkNotNull(errMessage, "ErrMessage must not be null (may be empty)");

            this.errMessage = errMessage;
        }

        public String getErrMessage() {
            return errMessage;
        }
    }
}