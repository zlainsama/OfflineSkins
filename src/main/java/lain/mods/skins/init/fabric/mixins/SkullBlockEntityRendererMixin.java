package lain.mods.skins.init.fabric.mixins;

import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin extends BlockEntityRenderer<SkullBlockEntity> {

    public SkullBlockEntityRendererMixin(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "method_3578(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/render/RenderLayer;", at = @At("RETURN"), cancellable = true, require = 0)
    private static void getRenderLayer_nBXjeY(SkullBlock.SkullType type, GameProfile profile, CallbackInfoReturnable<RenderLayer> info) {
        if (type == SkullBlock.Type.PLAYER && profile != null) {
            Identifier loc = FabricOfflineSkins.getLocationSkin(profile, null);
            if (loc != null)
                info.setReturnValue(RenderLayer.getEntityTranslucent(loc));
        }
    }

}
