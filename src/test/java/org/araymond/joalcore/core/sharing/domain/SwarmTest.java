package org.araymond.joalcore.core.sharing.domain;

import org.araymond.joalcore.core.sharing.domain.services.PeerElection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwarmTest {

    public Swarm.TrackerUniqueIdentifier tuid(String val) {
        return new Swarm.TrackerUniqueIdentifier(val);
    }
    public Peers peers(int leechers, int seeders) {
        return new Peers(new Leechers(leechers), new Seeders(seeders));
    }
    private Swarm defaultSwarm() {
        return Swarm.EMPTY
                .with(tuid("a"), peers(3, 25))
                .with(tuid("b"), peers(2500, 0))
                .with(tuid("c"), peers(241, 350))
                .with(tuid("d"), peers(25, 600))
                .with(tuid("e"), peers(0, 1453));
    }

    @Test
    public void shouldElectEmptyIfNoPeersRegistered() {
        var swarm = Swarm.EMPTY;

        assertThat(swarm.representativePeers((c1, c2) -> c1))
                .isEmpty();
    }

    @Test
    public void shouldElectIfOnlyOneEntry() {
        var swarm = Swarm.EMPTY
                .with(tuid("a"), peers(0, 0));

        assertThat(swarm.representativePeers((c1, c2) -> c1))
                .isNotEmpty()
                .contains(peers(0, 0));
    }

    @Test
    public void shouldElectMostLeeched() {
        var swarm = defaultSwarm();

        assertThat(swarm.representativePeers(PeerElection.MOST_LEECHED))
                .isNotEmpty()
                .contains(peers(2500, 0));
    }

    @Test
    public void shouldElectMostLeechedNonZeroSeeders() {
        var swarm = defaultSwarm();

        assertThat(swarm.representativePeers(PeerElection.MOST_LEECHED_NON_ZERO_SEEDERS))
                .isNotEmpty()
                .contains(peers(241, 350));
    }

    @Test
    public void shouldElectMostSeeded() {
        var swarm = defaultSwarm();

        assertThat(swarm.representativePeers(PeerElection.MOST_SEEDED))
                .isNotEmpty()
                .contains(peers(0, 1453));
    }

    @Test
    public void shouldElectMostSeededNonZeroLeechers() {
        var swarm = defaultSwarm();

        assertThat(swarm.representativePeers(PeerElection.MOST_SEEDED_NON_ZERO_LEECHERS))
                .isNotEmpty()
                .contains(peers(25, 600));
    }
}