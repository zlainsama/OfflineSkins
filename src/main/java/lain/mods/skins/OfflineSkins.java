package lain.mods.skins;

import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProviderService;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.providers.CrafatarCachedSkinProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "offlineskins", useMetadata = true)
public class OfflineSkins
{

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (!player.hasSkin() && service != null)
        {
            ISkin skin = service.getSkin(player);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (!player.hasSkin() && service != null)
        {
            ISkin skin = service.getSkin(player);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinType();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ISkinProviderService service;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            service = SkinProviderAPI.createService();

            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            service.register(new UserManagedSkinProvider());
            if (config.get(Configuration.CATEGORY_CLIENT, "useCrafatar", true).getBoolean(true))
                service.register(new CrafatarCachedSkinProvider());
            if (config.hasChanged())
                config.save();
        }
    }

}
