package gg.solarmc.loader.kitpvp;

interface KitCacheKey {
}

record KitKeyId(int id) implements KitCacheKey {
}

record KitKeyName(String name) implements KitCacheKey {
}