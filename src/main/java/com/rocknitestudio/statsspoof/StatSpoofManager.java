package com.rocknitestudio.statsspoof;

import net.minecraft.stats.Stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Stocke les valeurs "spoofees" par statistique.
 * Purement en memoire : rien n'est jamais ecrit dans stats/<uuid>.json,
 * donc rien n'affecte les advancements, le score, ou une future partie.
 *
 * Les instances de Stat<?> sont mises en cache par StatType#get(...), donc
 * la meme clé (ex: Stats.CUSTOM.get(Stats.JUMP)) revient toujours comme le
 * même objet -> on peut s'en servir tel quel comme clé de map.
 */
public final class StatSpoofManager {

    private static final Map<Stat<?>, Long> OVERRIDES = new HashMap<>();

    private StatSpoofManager() {
    }

    public static boolean hasOverride(Stat<?> stat) {
        return OVERRIDES.containsKey(stat);
    }

    public static long getOverride(Stat<?> stat) {
        Long value = OVERRIDES.get(stat);
        return value == null ? 0L : value;
    }

    public static void setOverride(Stat<?> stat, long value) {
        OVERRIDES.put(stat, value);
    }

    public static void clearOverride(Stat<?> stat) {
        OVERRIDES.remove(stat);
    }

    public static void clearAll() {
        OVERRIDES.clear();
    }

    public static int count() {
        return OVERRIDES.size();
    }
}
