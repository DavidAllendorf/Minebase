package de.xlb.minebase.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChangeData {
    private final Map<String, String> updates = new HashMap<>();

    public void put(String key, String value) {
        updates.put(key, value);
    }

    public String get(String key) {
        return updates.get(key);
    }

    public Map<String, String> getMap() {
        return updates;
    }

    public boolean containsKey(String key) {
        return updates.containsKey(key);
    }

    public boolean isEmpty() {
        return updates.isEmpty();
    }

    public Set<String> keySet() {
        return updates.keySet();
    }

    @Override
    public String toString() {
        return updates.toString();
    }
}
