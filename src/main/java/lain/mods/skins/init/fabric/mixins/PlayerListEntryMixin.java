package lain.mods.skins.init.fabric.mixins;

import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Final
    @Shadow
    private GameProfile profile;

    @Inject(method = "getCapeTexture", at = @At("RETURN"), cancellable = true)
    private void getCapeTexture(CallbackInfoReturnable<Identifier> info) {
        Identifier loc = FabricOfflineSkins.getLocationCape(this.profile, info.getReturnValue());
        if (loc != null)
            info.setReturnValue(loc);
    }

    @Inject(method = "getSkinTexture", at = @At("RETURN"), cancellable = true)
    private void getSkinTexture(CallbackInfoReturnable<Identifier> info) {
        Identifier loc = FabricOfflineSkins.getLocationSkin(this.profile, info.getReturnValue());
        if (loc != null)
            info.setReturnValue(loc);
    }

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
    private void getModel(CallbackInfoReturnable<String> info) {
        String type = FabricOfflineSkins.getSkinType(this.profile, info.getReturnValue());
        if (type != null)
            info.setReturnValue(type);
    }

}