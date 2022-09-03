package net.playlegend.cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import net.playlegend.LegendPerm;

abstract class Cache<K, V> {

    protected final LegendPerm plugin;

    public Cache(LegendPerm plugin) {
        this.plugin = plugin;
    }

    protected abstract void releaseAll();

    public abstract V get(K key) throws ExecutionException;

    public abstract ImmutableMap<K, V> getAll(Iterable<K> iterable) throws ExecutionException;

    public abstract void refresh(K key);

    public abstract void release(K key);

    public abstract void cleanup();

}
