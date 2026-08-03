package com.rocknitestudio.statsspoof;

import com.rocknitestudio.statsspoof.command.StatsSpoofCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public final class StatsSpoofClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                StatsSpoofCommand.register(dispatcher));
    }
}
