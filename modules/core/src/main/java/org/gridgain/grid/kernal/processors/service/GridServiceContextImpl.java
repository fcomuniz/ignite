/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.service;

import org.apache.ignite.managed.*;
import org.gridgain.grid.util.tostring.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service context implementation.
 */
public class GridServiceContextImpl implements GridServiceContext {
    /** */
    private static final long serialVersionUID = 0L;

    /** Null method. */
    private static final Method NULL_METHOD = GridServiceContextImpl.class.getMethods()[0];

    /** Service name. */
    private final String name;

    /** Execution ID. */
    private final UUID execId;

    /** Cache name. */
    private final String cacheName;

    /** Affinity key. */
    private final Object affKey;

    /** Service. */
    @GridToStringExclude
    private final GridService svc;

    /** Executor service. */
    @GridToStringExclude
    private final ExecutorService exe;

    /** Methods reflection cache. */
    private final ConcurrentMap<GridServiceMethodReflectKey, Method> mtds = new ConcurrentHashMap<>();

    /** Cancelled flag. */
    private volatile boolean isCancelled;


    /**
     * @param name Service name.
     * @param execId Execution ID.
     * @param cacheName Cache name.
     * @param affKey Affinity key.
     * @param svc Service.
     * @param exe Executor service.
     */
    GridServiceContextImpl(String name, UUID execId, String cacheName, Object affKey, GridService svc,
        ExecutorService exe) {
        this.name = name;
        this.execId = execId;
        this.cacheName = cacheName;
        this.affKey = affKey;
        this.svc = svc;
        this.exe = exe;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override public UUID executionId() {
        return execId;
    }

    /** {@inheritDoc} */
    @Override public boolean isCancelled() {
        return isCancelled;
    }

    /** {@inheritDoc} */
    @Nullable @Override public String cacheName() {
        return cacheName;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Nullable @Override public <K> K affinityKey() {
        return (K)affKey;
    }

    /**
     * @return Service instance.
     */
    GridService service() {
        return svc;
    }

    /**
     * @return Executor service.
     */
    ExecutorService executor() {
        return exe;
    }

    /**
     * @param key Method key.
     * @return Method.
     */
    @Nullable Method method(GridServiceMethodReflectKey key) {
        Method mtd = mtds.get(key);

        if (mtd == null) {
            try {
                mtd = svc.getClass().getMethod(key.methodName(), key.argTypes());
            }
            catch (NoSuchMethodException e) {
                mtd = NULL_METHOD;
            }

            mtds.put(key, mtd);
        }

        return mtd == NULL_METHOD ? null : mtd;
    }

    /**
     * @param isCancelled Cancelled flag.
     */
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridServiceContextImpl.class, this);
    }
}
