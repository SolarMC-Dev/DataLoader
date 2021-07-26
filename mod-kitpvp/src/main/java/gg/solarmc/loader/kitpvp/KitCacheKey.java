package gg.solarmc.loader.kitpvp;

import java.util.Objects;

public class KitCacheKey {
    private final Integer id;
    private final String name;

    public KitCacheKey(int id) {
        this(id, null);
    }

    /**
     * @param name should not be null
     */
    public KitCacheKey(String name) {
        this(null, name);
    }

    public KitCacheKey(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof KitCacheKey key)) return false;

        if (key.id == null) return this.name.equals(key.name);
        if (key.name == null) return this.id.equals(key.id);

        return id.equals(key.id) || name.equals(key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
