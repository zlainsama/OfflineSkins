package lain.mods.skins.asm;

import com.mojang.authlib.GameProfile;
import lain.mods.skins.OfflineSkins;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

public class Hooks
{

    private static final boolean DISABLED = !Loader.isModLoaded("offlineskins");

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

    public static int getSkinHeight(ResourceLocation location)
    {
        if (DISABLED)
            return 64;
        return OfflineSkins.getSkinHeight(location);
    }

    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (DISABLED)
            return result;
        return OfflineSkins.getSkinType(player, result);
    }

    public static int getSkinWidth(ResourceLocation location)
    {
        if (DISABLED)
            return 64;
        return OfflineSkins.getSkinWidth(location);
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

}
