package org.araymond.joal.core.ttorrent.client.announcer.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SuccessAnnounceResponse {

    private final int interval;
    private final int seeders;
    private final int leechers;
}
