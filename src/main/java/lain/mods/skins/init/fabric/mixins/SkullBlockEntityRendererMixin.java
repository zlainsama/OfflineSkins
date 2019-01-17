package lain.mods.skins.init.fabric.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.util.Identifier;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin extends BlockEntityRenderer<SkullBlockEntity>
{

    @Inject(method = "method_3578(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getLocationTexture_nBXjeY(SkullBlock.SkullType type, GameProfile profile, CallbackInfoReturnable<Identifier> info)
    {
        if (type == SkullBlock.Type.PLAYER && profile != null)
        {
            Identifier loc = FabricOfflineSkins.getLocationSkin(profile, info.getReturnValue());
            if (loc != null)
                info.setReturnValue(loc);
        }
    }

}
