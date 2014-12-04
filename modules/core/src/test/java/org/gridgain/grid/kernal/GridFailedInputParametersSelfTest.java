/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.gridgain.testframework.junits.common.*;

import static org.gridgain.grid.events.GridEventType.*;

/**
 * Test for invalid input parameters.
 */
@GridCommonTest(group = "Kernal Self")
public class GridFailedInputParametersSelfTest extends GridCommonAbstractTest {
    /** */
    private static Ignite ignite;

    /** */
    public GridFailedInputParametersSelfTest() {
        super(/*start grid*/true);
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        ignite = G.grid(getTestGridName());
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testAddEventLocalListener() throws Exception {
        try {
            ignite.events().localListen(null, EVTS_ALL);

            assert false : "Null listener can't be added.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testRemoveEventLocalListener() throws Exception {
        try {
            ignite.events().stopLocalListen(null);

            assert false : "Null listener can't be removed.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testAddDiscoveryListener() throws Exception {
        try {
            ignite.events().localListen(null, EVTS_ALL);

            assert false : "Null listener can't be added.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testRemoveDiscoveryListener() throws Exception {
        try {
            ignite.events().stopLocalListen(null);

            assert false : "Null listener can't be removed.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testGetNode() throws Exception {
        try {
            ignite.cluster().node(null);

            assert false : "Null nodeId can't be entered.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testPingNode() throws Exception {
        try {
            ignite.cluster().pingNode(null);

            assert false : "Null nodeId can't be entered.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }
    }

    /**
     * @throws Exception Thrown in case of any errors.
     */
    public void testDeployTask() throws Exception {
        try {
            ignite.compute().localDeployTask(null, null);

            assert false : "Null task can't be entered.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }

        try {
            ignite.compute().localDeployTask(null, null);

            assert false : "Null task can't be entered.";
        }
        catch (NullPointerException ignored) {
            // No-op.
        }

        // Check for exceptions.
        ignite.compute().localDeployTask(GridTestTask.class, U.detectClassLoader(GridTestTask.class));
    }
}
