package lain.mods.skins.asm;

import lain.mods.skins.OfflineSkins;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import com.mojang.authlib.GameProfile;

public class Hooks
{

    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        return OfflineSkins.getLocationCape(player, result);
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        return OfflineSkins.getLocationSkin(player, result);
    }

    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        return OfflineSkins.getSkinType(player, result);
    }

    public static ResourceLocation GuiPlayerTabOverlay_bindTexture(GameProfile profile, ResourceLocation result)
    {
        return OfflineSkins.bindTexture(profile, result);
    }

    public static ResourceLocation TileEntitySkullRenderer_bindTexture(GameProfile profile, ResourceLocation result)
    {
        return OfflineSkins.bindTexture(profile, result);
    }

}
