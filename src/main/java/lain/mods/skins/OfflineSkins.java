package lain.mods.skins;

import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProviderService;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.providers.CrafatarCachedCapeProvider;
import lain.mods.skins.providers.CrafatarCachedSkinProvider;
import lain.mods.skins.providers.MojangCachedCapeProvider;
import lain.mods.skins.providers.MojangCachedSkinProvider;
import lain.mods.skins.providers.UserManagedCapeProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "offlineskins", useMetadata = true, acceptedMinecraftVersions = "[1.10]")
public class OfflineSkins
{

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (result == null && capeService != null)
        {
            ISkin cape = capeService.getSkin(player);
            if (cape != null && cape.isSkinReady())
                return cape.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (SkinPass)
            return result;

        if (isDefaultSkin(player) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (isDefaultSkin(player) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinType();
        }
        return result;
    }

    public static boolean isDefaultSkin(AbstractClientPlayer player)
    {
        try
        {
            SkinPass = true;
            return SkinData.isDefaultSkin(player.getLocationSkin());
        }
        finally
        {
            SkinPass = false;
        }
    }

    private static boolean SkinPass = false;

    @SideOnly(Side.CLIENT)
    public static ISkinProviderService skinService;
    @SideOnly(Side.CLIENT)
    public static ISkinProviderService capeService;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleClientTicks(TickEvent.ClientTickEvent event)
    {
        if (skinService == null && capeService == null)
            return;

        if (event.phase == TickEvent.Phase.START)
        {
            World world = Minecraft.getMinecraft().theWorld;
            if (world != null && world.playerEntities != null && !world.playerEntities.isEmpty())
            {
                for (Object obj : world.playerEntities)
                {
                    // This should keep skins/capes loaded.
                    if (obj instanceof AbstractClientPlayer)
                    {
                        if (skinService != null)
                            skinService.getSkin((AbstractClientPlayer) obj);
                        if (capeService != null)
                            capeService.getSkin((AbstractClientPlayer) obj);
                    }
                }
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            boolean useCrafatar = config.get(Configuration.CATEGORY_CLIENT, "useCrafatar", true).getBoolean(true);
            if (config.hasChanged())
                config.save();

            skinService = SkinProviderAPI.createService();
            capeService = SkinProviderAPI.createService();

            skinService.register(new MojangCachedSkinProvider());
            skinService.register(new UserManagedSkinProvider());
            if (useCrafatar)
                skinService.register(new CrafatarCachedSkinProvider());
            capeService.register(new MojangCachedCapeProvider());
            capeService.register(new UserManagedCapeProvider());
            if (useCrafatar)
                capeService.register(new CrafatarCachedCapeProvider());

            MinecraftForge.EVENT_BUS.register(this);
        }
    }

}
