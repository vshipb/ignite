/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.messaging.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Test for {@link org.gridgain.grid.Ignite}.
 */
@GridCommonTest(group = "Kernal Self")
public class GridSelfTest extends GridProjectionAbstractTest {
    /** Nodes count. */
    private static final int NODES_CNT = 4;

    /** {@inheritDoc} */
    @SuppressWarnings({"ConstantConditions"})
    @Override protected void beforeTestsStarted() throws Exception {
        assert NODES_CNT > 2;

        for (int i = 0; i < NODES_CNT; i++)
            startGrid(i);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected GridProjection projection() {
        return grid(0);
    }

    /** {@inheritDoc} */
    @Override protected UUID localNodeId() {
        return grid(0).localNode().id();
    }

    /** {@inheritDoc} */
    @Override protected Collection<UUID> remoteNodeIds() {
        return F.nodeIds(grid(0).forRemotes().nodes());
    }

    /** {@inheritDoc} */
    @Override public void testRemoteNodes() throws Exception {
        int size = remoteNodeIds().size();

        String name = "oneMoreGrid";

        try {
            Ignite g = startGrid(name);

            UUID joinedId = g.cluster().localNode().id();

            assert projection().forRemotes().nodes().size() == size + 1;

            assert F.nodeIds(projection().forRemotes().nodes()).contains(joinedId);
        }
        finally {
            stopGrid(name);
        }
    }

    /** {@inheritDoc} */
    @Override public void testRemoteProjection() throws Exception {
        GridProjection remotePrj = projection().forRemotes();

        int size = remotePrj.nodes().size();

        String name = "oneMoreGrid";

        try {
            Ignite g = startGrid(name);

            UUID joinedId = g.cluster().localNode().id();

            assert remotePrj.nodes().size() == size + 1;

            assert F.nodeIds(remotePrj.nodes()).contains(joinedId);
        }
        finally {
            stopGrid(name);
        }
    }

    /**
     * @throws Exception If failed.
     */
    @SuppressWarnings({"TooBroadScope"})
    public void testAsyncListen() throws Exception {
        final String msg = "HELLO!";

        Ignite g = (Ignite)projection();

        final UUID locNodeId = g.cluster().localNode().id();

        g.message().remoteListen(null, new GridMessagingListenActor<String>() {
            @Override protected void receive(UUID nodeId, String rcvMsg) throws Throwable {
                assert locNodeId.equals(nodeId);
                assert msg.equals(rcvMsg);

                stop(rcvMsg);
            }
        });

        final AtomicInteger cnt = new AtomicInteger();

        g.message().localListen(null, new P2<UUID, String>() {
            @Override
            public boolean apply(UUID nodeId, String msg) {
                if (!locNodeId.equals(nodeId))
                    cnt.incrementAndGet();

                return true;
            }
        });

        g.message().send(null, msg);

        Thread.sleep(1000);

        assert cnt.get() == g.cluster().forRemotes().nodes().size();
    }

    /**
     * @throws Exception If failed.
     */
    public void testForOthers() throws Exception {
        GridNode node0 = grid(0).localNode();
        GridNode node1 = grid(1).localNode();
        GridNode node2 = grid(2).localNode();
        GridNode node3 = grid(3).localNode();

        GridProjection p1 = grid(0).forOthers(node0);

        assertEquals(3, p1.nodes().size());

        assertEquals(2, p1.forOthers(node1).nodes().size());

        assertEquals(1, p1.forOthers(node1, node2).nodes().size());

        assertEquals(1, grid(0).forOthers(node1, node2, node3).nodes().size());
    }
}
