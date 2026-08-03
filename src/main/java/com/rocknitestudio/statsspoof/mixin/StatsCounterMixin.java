package com.rocknitestudio.statsspoof.mixin;

import com.rocknitestudio.statsspoof.StatSpoofManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StatsScreen (et le HUD) passent tous par StatsCounter#getValue(Stat<?>) pour savoir
 * quoi afficher -> c'est le seul point qu'on doit intercepter, peu importe l'onglet
 * (General / Items / Mobs) ou la structure interne (privee, souvent en lambda) de
 * StatsScreen elle-meme, qui change trop souvent d'une version a l'autre pour etre
 * une cible de mixin fiable.
 *
 * Garde-fou important : on ne spoof QUE le StatsCounter du joueur local cote client
 * (celui utilise pour l'affichage/HUD). Le StatsCounter du serveur integre (utilise
 * pour les advancements et la sauvegarde du monde en solo) est une toute autre
 * instance en memoire, meme en solo -> elle n'est jamais concernee par ce check.
 */
@Mixin(StatsCounter.class)
public abstract class StatsCounterMixin {

    @Inject(method = "getValue(Lnet/minecraft/stats/Stat;)I", at = @At("RETURN"), cancellable = true)
    private void statsspoof$overrideDisplayedValue(Stat<?> stat, CallbackInfoReturnable<Integer> cir) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        // (Object) cast necessaire : StatsCounter n'a pas de "equals" custom, on compare
        // volontairement par identite pour ne toucher qu'a la copie client du joueur local.
        if ((Object) this != player.getStats()) {
            return;
        }
        if (StatSpoofManager.hasOverride(stat)) {
            cir.setReturnValue((int) StatSpoofManager.getOverride(stat));
        }
    }
}
