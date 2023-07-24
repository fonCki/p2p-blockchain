package utils;

import java.util.concurrent.ConcurrentHashMap;

public class ObservableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
    private Runnable callback;

    public ObservableConcurrentHashMap(Runnable callback) {
        super();
        this.callback = callback;
    }

    @Override
    public V put(K key, V value) {
        V result = super.put(key, value);
        callback.run();
        return result;
    }

    @Override
    public V remove(Object key) {
        V result = super.remove(key);
        callback.run();
        return result;
    }

    // Add similar overrides for other methods that modify the map
}
