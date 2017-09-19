package lain.mods.skins.asm;

import lain.mods.skins.OfflineSkins;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import com.mojang.authlib.GameProfile;

public class Hooks
{

    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.getLocationCape(player, result);
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.getLocationSkin(player, result);
    }

    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.getSkinType(player, result);
    }

    public static ResourceLocation GuiPlayerTabOverlay_bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.bindTexture(profile, result);
    }

    public static ResourceLocation TileEntitySkullRenderer_bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.bindTexture(profile, result);
    }

    private static final boolean DISABLED = !Loader.isModLoaded("offlineskins");

}
