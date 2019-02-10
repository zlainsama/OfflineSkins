package lain.mods.skins.init.forge.asm;

import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.forge.ForgeOfflineSkins;
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
        ResourceLocation loc = ForgeOfflineSkins.getLocationCape(player, result);
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        ResourceLocation loc = ForgeOfflineSkins.getLocationSkin(player, result);
        if (loc != null)
            return loc;
        return result;
    }

    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (DISABLED)
            return result;
        String type = ForgeOfflineSkins.getSkinType(player, result);
        if (type != null)
            return type;
        return result;
    }

    public static ResourceLocation GuiPlayerTabOverlay_bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        ResourceLocation loc = ForgeOfflineSkins.bindTexture(profile, result);
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation TileEntitySkullRenderer_bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (DISABLED)
            return result;
        ResourceLocation loc = ForgeOfflineSkins.bindTexture(profile, result);
        if (loc != null)
            return loc;
        return result;
    }

}
