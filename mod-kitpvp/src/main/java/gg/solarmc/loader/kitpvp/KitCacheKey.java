package gg.solarmc.loader.kitpvp;

import java.util.Locale;

interface KitCacheKey {
}

record KitKeyId(int id) implements KitCacheKey {
}

record KitKeyName(String name) implements KitCacheKey {
    KitKeyName {
        name = name.toLowerCase(Locale.ROOT);
    }
}