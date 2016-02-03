package lain.mods.skins.asm;

import lain.mods.skins.OfflineSkins;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

public class Hooks
{

    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        return OfflineSkins.getLocationSkin(player, result);
    }

    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        return OfflineSkins.getSkinType(player, result);
    }

}
