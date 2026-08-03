package com.rocknitestudio.statsspoof.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.rocknitestudio.statsspoof.StatKeyResolver;
import com.rocknitestudio.statsspoof.StatSpoofManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;

public final class StatsSpoofCommand {

    private enum Operation { SET, ADD, REMOVE }

    private StatsSpoofCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("statsspoof")
                .then(ClientCommands.literal("set").then(withValue(Operation.SET)))
                .then(ClientCommands.literal("add").then(withValue(Operation.ADD)))
                .then(ClientCommands.literal("remove").then(withValue(Operation.REMOVE)))
                .then(ClientCommands.literal("clear")
                        .then(ClientCommands.argument("type", StringArgumentType.word())
                                .then(ClientCommands.argument("id", StringArgumentType.string())
                                        .executes(StatsSpoofCommand::executeClear))))
                .then(ClientCommands.literal("clearall")
                        .executes(StatsSpoofCommand::executeClearAll))
        );
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, String> withValue(Operation op) {
        return ClientCommands.argument("type", StringArgumentType.word())
                .then(ClientCommands.argument("id", StringArgumentType.string())
                        .then(ClientCommands.argument("valeur", LongArgumentType.longArg())
                                .executes(ctx -> execute(ctx, op))));
    }

    private static int execute(CommandContext<FabricClientCommandSource> ctx, Operation op) throws CommandSyntaxException {
        String type = StringArgumentType.getString(ctx, "type");
        String id = StringArgumentType.getString(ctx, "id");
        long value = LongArgumentType.getLong(ctx, "valeur");

        FabricClientCommandSource source = ctx.getSource();
        LocalPlayer player = source.getPlayer();
        Stat<?> stat = StatKeyResolver.resolve(type, id);

        long current = player.getStats().getValue(stat);
        long updated = switch (op) {
            case SET -> value;
            case ADD -> current + value;
            case REMOVE -> current - value;
        };

        StatSpoofManager.setOverride(stat, updated);
        source.sendFeedback(Component.literal(
                "[StatsSpoof] " + type + "/" + id + " -> " + updated + " (affichage uniquement)"));
        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String type = StringArgumentType.getString(ctx, "type");
        String id = StringArgumentType.getString(ctx, "id");
        Stat<?> stat = StatKeyResolver.resolve(type, id);
        StatSpoofManager.clearOverride(stat);
        ctx.getSource().sendFeedback(Component.literal(
                "[StatsSpoof] " + type + "/" + id + " : valeur reelle restauree"));
        return 1;
    }

    private static int executeClearAll(CommandContext<FabricClientCommandSource> ctx) {
        int removed = StatSpoofManager.count();
        StatSpoofManager.clearAll();
        ctx.getSource().sendFeedback(Component.literal(
                "[StatsSpoof] " + removed + " override(s) retire(s), toutes les stats affichent a nouveau leur vraie valeur"));
        return 1;
    }
}
