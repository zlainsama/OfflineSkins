package lain.mods.skins.init.fabric.mixins;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.ScoreboardHud;

@Mixin(ScoreboardHud.class)
public abstract class ScoreboardHudMixin extends Drawable
{

    @ModifyVariable(method = "method_1919(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "STORE", opcode=Opcodes.ISTORE, ordinal = 0))
    private boolean onDrawSetFlag_nBXjeY(boolean result)
    {
        return true;
    }

}
