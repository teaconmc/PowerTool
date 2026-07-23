package org.teacon.powertool.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SizedCache<K,V> {
    
    private int size = 16;
    private final Map<K,V> cache = new HashMap<>();
    private final Set<K> lastTickUsed = new HashSet<>();
    
    public SizedCache(){
    }
    
    public SizedCache(int size) {
        this.size = size;
    }
    
    public V getOrCreate(K key, Function<K,V> kvFunction){
        lastTickUsed.add(key);
        return cache.computeIfAbsent(key, kvFunction);
    }
    
    public Map<K,V> getMap(){
        return cache;
    }
    
    public void tick(){
        if(cache.size() >= size){
            var iter = cache.entrySet().iterator();
            while(iter.hasNext()){
                var entry = iter.next();
                if (!lastTickUsed.contains(entry.getKey())) {
                    if(entry.getValue() instanceof AutoCloseable autoCloseable){
                        try {
                            autoCloseable.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    iter.remove();
                }
            }
        }
        lastTickUsed.clear();
    }
}
