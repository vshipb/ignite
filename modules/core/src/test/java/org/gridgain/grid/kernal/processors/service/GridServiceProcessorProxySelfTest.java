/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.service;

import org.gridgain.grid.*;
import org.gridgain.grid.service.*;
import org.gridgain.grid.util.typedef.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Service proxy test.
 */
public class GridServiceProcessorProxySelfTest extends GridServiceProcessorAbstractSelfTest {

    /** {@inheritDoc} */
    @Override protected int nodeCount() {
        return 4;
    }

    /**
     * @throws Exception If failed.
     */
    public void testNodeSingletonProxy() throws Exception {
        String name = "testNodeSingletonProxy";

        Ignite ignite = randomGrid();

        ignite.services().deployNodeSingleton(name, new CounterServiceImpl());

        CounterService svc = ignite.services().serviceProxy(name, CounterService.class, false);

        for (int i = 0; i < 10; i++)
            svc.increment();

        assertEquals(10, svc.get());
        assertEquals(10, svc.localIncrements());
        assertEquals(10, ignite.services(ignite.cluster().forLocal()).
            serviceProxy(name, CounterService.class, false).localIncrements());

        // Make sure that remote proxies were not called.
        for (GridNode n : ignite.cluster().forRemotes().nodes()) {
            CounterService rmtSvc =
                    ignite.services(ignite.cluster().forNode(n)).serviceProxy(name, CounterService.class, false);

            assertEquals(0, rmtSvc.localIncrements());
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testClusterSingletonProxy() throws Exception {
        String name = "testClusterSingletonProxy";

        Ignite ignite = randomGrid();

        ignite.services().deployClusterSingleton(name, new CounterServiceImpl());

        CounterService svc = ignite.services().serviceProxy(name, CounterService.class, true);

        for (int i = 0; i < 10; i++)
            svc.increment();

        assertEquals(10, svc.get());
    }

    /**
     * @throws Exception If failed.
     */
    public void testMultiNodeProxy() throws Exception {
        Ignite ignite = randomGrid();

        int extras = 3;

        startExtraNodes(extras);

        String name = "testMultiNodeProxy";

        ignite.services().deployNodeSingleton(name, new CounterServiceImpl());

        CounterService svc = ignite.services().serviceProxy(name, CounterService.class, false);

        for (int i = 0; i < extras; i++) {
            svc.increment();

            stopGrid(nodeCount() + i);
        }

        assertEquals(extras, svc.get());
    }

    /**
     * @throws Exception If failed.
     */
    public void testNodeSingletonRemoteNotStickyProxy() throws Exception {
        String name = "testNodeSingletonRemoteNotStickyProxy";

        Ignite ignite = randomGrid();

        // Deploy only on remote nodes.
        ignite.services(ignite.cluster().forRemotes()).deployNodeSingleton(name, new CounterServiceImpl());

        info("Deployed service: " + name);

        // Get local proxy.
        CounterService svc = ignite.services().serviceProxy(name, CounterService.class, false);

        for (int i = 0; i < 10; i++)
            svc.increment();

        assertEquals(10, svc.get());

        int total = 0;

        for (GridNode n : ignite.cluster().forRemotes().nodes()) {
            CounterService rmtSvc =
                    ignite.services(ignite.cluster().forNode(n)).serviceProxy(name, CounterService.class, false);

            int cnt = rmtSvc.localIncrements();

            // Since deployment is not stick, count on each node must be less than 10.
            assertTrue("Invalid local increments: " + cnt, cnt != 10);

            total += cnt;
        }

        assertEquals(10, total);
    }

    /**
     * @throws Exception If failed.
     */
    public void testNodeSingletonRemoteStickyProxy() throws Exception {
        String name = "testNodeSingletonRemoteStickyProxy";

        Ignite ignite = randomGrid();

        // Deploy only on remote nodes.
        ignite.services(ignite.cluster().forRemotes()).deployNodeSingleton(name, new CounterServiceImpl());

        // Get local proxy.
        CounterService svc = ignite.services().serviceProxy(name, CounterService.class, true);

        for (int i = 0; i < 10; i++)
            svc.increment();

        assertEquals(10, svc.get());

        int total = 0;

        for (GridNode n : ignite.cluster().forRemotes().nodes()) {
            CounterService rmtSvc =
                    ignite.services(ignite.cluster().forNode(n)).serviceProxy(name, CounterService.class, false);

            int cnt = rmtSvc.localIncrements();

            assertTrue("Invalid local increments: " + cnt, cnt == 10 || cnt == 0);

            total += rmtSvc.localIncrements();
        }

        assertEquals(10, total);
    }

    /**
     * @throws Exception If failed.
     */
    public void testSingletonProxyInvocation() throws Exception {
        final String name = "testProxyInvocationFromSeveralNodes";

        final Ignite ignite = grid(0);

        ignite.services(ignite.cluster().forLocal()).deployClusterSingleton(name, new MapServiceImpl<String, Integer>());

        for (int i = 1; i < nodeCount(); i++) {
            MapService<Integer, String> svc =  grid(i).services().serviceProxy(name, MapService.class, false);

            // Make sure service is a proxy.
            assertFalse(svc instanceof GridService);

            svc.put(i, Integer.toString(i));
        }

        assertEquals(nodeCount() - 1, ignite.services().serviceProxy(name, MapService.class, false).size());
    }

    /**
     * @throws Exception If failed.
     */
    public void testLocalProxyInvocation() throws Exception {
        final String name = "testLocalProxyInvocation";

        final Ignite ignite = grid(0);

        ignite.services().deployNodeSingleton(name, new MapServiceImpl<String, Integer>());

        for (int i = 0; i < nodeCount(); i++) {
            MapService<Integer, String> svc =  grid(i).services().serviceProxy(name, MapService.class, false);

            // Make sure service is a local instance.
            assertTrue(svc instanceof GridService);

            svc.put(i, Integer.toString(i));
        }

        MapService<Integer, String> map = ignite.services().serviceProxy(name, MapService.class, false);

        for (int i = 0; i < nodeCount(); i++)
            assertEquals(1, map.size());
    }

    /**
     * @throws Exception If failed.
     */
    public void testRemoteNotStickProxyInvocation() throws Exception {
        final String name = "testRemoteNotStickProxyInvocation";

        final Ignite ignite = grid(0);

        ignite.services().deployNodeSingleton(name, new MapServiceImpl<String, Integer>());

        // Get remote proxy.
        MapService<Integer, String> svc =  ignite.services(ignite.cluster().forRemotes()).
            serviceProxy(name, MapService.class, false);

        // Make sure service is a local instance.
        assertFalse(svc instanceof GridService);

        for (int i = 0; i < nodeCount(); i++)
            svc.put(i, Integer.toString(i));

        int size = 0;

        for (GridNode n : ignite.cluster().forRemotes().nodes()) {
            MapService<Integer, String> map = ignite.services(ignite.cluster().forNode(n)).
                serviceProxy(name, MapService.class, false);

            // Make sure service is a local instance.
            assertFalse(map instanceof GridService);

            size += map.size();
        }

        assertEquals(nodeCount(), size);
    }

    /**
     * @throws Exception If failed.
     */
    public void testRemoteStickyProxyInvocation() throws Exception {
        final String name = "testRemoteStickyProxyInvocation";

        final Ignite ignite = grid(0);

        ignite.services().deployNodeSingleton(name, new MapServiceImpl<String, Integer>());

        // Get remote proxy.
        MapService<Integer, String> svc =  ignite.services(ignite.cluster().forRemotes()).
            serviceProxy(name, MapService.class, true);

        // Make sure service is a local instance.
        assertFalse(svc instanceof GridService);

        for (int i = 0; i < nodeCount(); i++)
            svc.put(i, Integer.toString(i));

        int size = 0;

        for (GridNode n : ignite.cluster().forRemotes().nodes()) {
            MapService<Integer, String> map = ignite.services(ignite.cluster().forNode(n)).
                serviceProxy(name, MapService.class, false);

            // Make sure service is a local instance.
            assertFalse(map instanceof GridService);

            if (map.size() != 0)
                size += map.size();
        }

        assertEquals(nodeCount(), size);
    }

    /**
     * Simple map service.
     *
     * @param <K> Type of cache keys.
     * @param <V> Type of cache values.
     */
    protected interface MapService<K, V> {
        /**
         * Puts key-value pair into map.
         *
         * @param key Key.
         * @param val Value.
         */
        void put(K key, V val);

        /**
         * Gets value based on key.
         *
         * @param key Key.
         * @return Value.
         */
        V get(K key);

        /**
         * Clears map.
         */
        void clear();

        /**
         * @return Map size.
         */
        int size();
    }

    /**
     * Cache service implementation.
     */
    protected static class MapServiceImpl<K, V> implements MapService<K, V>, GridService {
        /** Underlying cache map. */
        private final Map<K, V> map = new ConcurrentHashMap<>();

        /** {@inheritDoc} */
        @Override public void put(K key, V val) {
            map.put(key, val);
        }

        /** {@inheritDoc} */
        @Override public V get(K key) {
            return map.get(key);
        }

        /** {@inheritDoc} */
        @Override public void clear() {
            map.clear();
        }

        @Override public int size() {
            return map.size();
        }

        /** {@inheritDoc} */
        @Override public void cancel(GridServiceContext ctx) {
            X.println("Stopping cache service: " + ctx.name());
        }

        /** {@inheritDoc} */
        @Override public void init(GridServiceContext ctx) throws Exception {
            X.println("Initializing counter service: " + ctx.name());
        }

        /** {@inheritDoc} */
        @Override public void execute(GridServiceContext ctx) throws Exception {
            X.println("Executing cache service: " + ctx.name());
        }
    }
}
