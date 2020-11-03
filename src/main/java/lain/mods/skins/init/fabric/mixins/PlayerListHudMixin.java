package lain.mods.skins.init.fabric.mixins;

import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin extends DrawableHelper {

    @Shadow
    private MinecraftClient client;

    // (CallbackInfo info, PlayerListEntry entry, GameProfile profile)
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "INVOKE", target = "net.minecraft.client.texture.TextureManager.bindTexture(Lnet/minecraft/util/Identifier;)V", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void onRenderBindTexture_nBXjeY(MatrixStack var1, int var2, Scoreboard var3, ScoreboardObjective var4, CallbackInfo info, ClientPlayNetworkHandler var5, List<?> var6, int var7, int var8, int var9, int var10, int var11, boolean var12, int var13, int var14, int var15, int var16, int var17, List<?> var18, List<?> var19, int var20, int var21, int var22, int var23, int var24, int var25, PlayerListEntry entry, GameProfile profile, PlayerEntity var28, boolean var29) {
        Identifier loc = FabricOfflineSkins.getLocationSkin(profile, entry.getSkinTexture());
        if (loc != null)
            client.getTextureManager().bindTexture(loc);
    }

    @ModifyVariable(method = "render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "STORE", opcode = Opcodes.ISTORE, ordinal = 0), require = 0)
    private boolean onRenderSetFlag_nBXjeY(boolean result) {
        return true;
    }

}
