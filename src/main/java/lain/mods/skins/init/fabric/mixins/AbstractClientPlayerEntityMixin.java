package lain.mods.skins.init.fabric.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity
{

    public AbstractClientPlayerEntityMixin(World world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "method_3119()Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getLocationCape_nBXjeY(CallbackInfoReturnable<Identifier> info)
    {
        Identifier loc = FabricOfflineSkins.getLocationCape(getGameProfile(), info.getReturnValue());
        if (loc != null)
            info.setReturnValue(loc);
    }

    @Inject(method = "getSkinTexture()Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getLocationSkin_nBXjeY(CallbackInfoReturnable<Identifier> info)
    {
        Identifier loc = FabricOfflineSkins.getLocationSkin(getGameProfile(), info.getReturnValue());
        if (loc != null)
            info.setReturnValue(loc);
    }

    @Inject(method = "method_3121()Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void getSkinType_nBXjeY(CallbackInfoReturnable<String> info)
    {
        String type = FabricOfflineSkins.getSkinType(getGameProfile(), info.getReturnValue());
        if (type != null)
            info.setReturnValue(type);
    }

}
