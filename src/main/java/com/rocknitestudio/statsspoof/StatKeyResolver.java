package com.rocknitestudio.statsspoof;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Traduit "<type> <id>" (ex: "custom jump", "mined stone", "killed zombie")
 * en un vrai Stat<?> vanilla, sans avoir a se soucier de l'obfuscation
 * puisque 26.2 est distribue avec les noms officiels directement.
 */
public final class StatKeyResolver {

    private static final DynamicCommandExceptionType UNKNOWN_TYPE =
            new DynamicCommandExceptionType(o -> Component.literal("Type de stat inconnu : " + o
                    + " (attendu : custom, mined, crafted, used, broken, picked_up, dropped, killed, killed_by)"));

    private static final DynamicCommandExceptionType UNKNOWN_BLOCK =
            new DynamicCommandExceptionType(o -> Component.literal("Bloc inconnu : " + o));
    private static final DynamicCommandExceptionType UNKNOWN_ITEM =
            new DynamicCommandExceptionType(o -> Component.literal("Item inconnu : " + o));
    private static final DynamicCommandExceptionType UNKNOWN_ENTITY =
            new DynamicCommandExceptionType(o -> Component.literal("Type d'entite inconnu : " + o));
    private static final DynamicCommandExceptionType UNKNOWN_CUSTOM_STAT =
            new DynamicCommandExceptionType(o -> Component.literal("Stat custom inconnue : " + o));

    private StatKeyResolver() {
    }

    public static Stat<?> resolve(String type, String rawId) throws CommandSyntaxException {
        Identifier id = parseId(rawId);
        return switch (type) {
            case "custom" -> Stats.CUSTOM.get(requireCustomStat(id));
            case "mined" -> Stats.BLOCK_MINED.get(requireBlock(id));
            case "crafted" -> Stats.ITEM_CRAFTED.get(requireItem(id));
            case "used" -> Stats.ITEM_USED.get(requireItem(id));
            case "broken" -> Stats.ITEM_BROKEN.get(requireItem(id));
            case "picked_up" -> Stats.ITEM_PICKED_UP.get(requireItem(id));
            case "dropped" -> Stats.ITEM_DROPPED.get(requireItem(id));
            case "killed" -> Stats.ENTITY_KILLED.get(requireEntityType(id));
            case "killed_by" -> Stats.ENTITY_KILLED_BY.get(requireEntityType(id));
            default -> throw UNKNOWN_TYPE.create(type);
        };
    }

    /** Accepte aussi bien "jump" (=> minecraft:jump) que "monmod:un_truc". */
    private static Identifier parseId(String raw) {
        String full = raw.contains(":") ? raw : "minecraft:" + raw;
        return Identifier.parse(full);
    }

    private static Block requireBlock(Identifier id) throws CommandSyntaxException {
        return BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(() -> UNKNOWN_BLOCK.create(id));
    }

    private static Identifier requireCustomStat(Identifier id) throws CommandSyntaxException {
        return BuiltInRegistries.CUSTOM_STAT.getOptional(id).orElseThrow(() -> UNKNOWN_CUSTOM_STAT.create(id));
    }

    private static Item requireItem(Identifier id) throws CommandSyntaxException {
        return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> UNKNOWN_ITEM.create(id));
    }

    private static EntityType<?> requireEntityType(Identifier id) throws CommandSyntaxException {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow(() -> UNKNOWN_ENTITY.create(id));
    }
}
