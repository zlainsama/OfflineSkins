package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProviderService;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.providers.CrafatarCachedCapeProvider;
import lain.mods.skins.providers.CrafatarCachedSkinProvider;
import lain.mods.skins.providers.CustomServerCachedCapeProvider;
import lain.mods.skins.providers.CustomServerCachedSkinProvider;
import lain.mods.skins.providers.MojangCachedCapeProvider;
import lain.mods.skins.providers.MojangCachedSkinProvider;
import lain.mods.skins.providers.UserManagedCapeProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mojang.authlib.GameProfile;

@Mod(modid = "offlineskins", useMetadata = true, acceptedMinecraftVersions = "[1.12, 1.13)", certificateFingerprint = "aaaf83332a11df02406e9f266b1b65c1306f0f76")
public class OfflineSkins
{

    @SideOnly(Side.CLIENT)
    public static ResourceLocation bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (SkinData.isDefaultSkin(result) && profile != null)
        {
            ISkin skin = skinService.getSkin(profile);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (CapePass)
            return result;

        if ((OverrideVanilla || usingDefaultCape(player)) && capeService != null)
        {
            ISkin cape = capeService.getSkin(player.getGameProfile());
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

        if ((OverrideVanilla || usingDefaultSkin(player)) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player.getGameProfile());
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (SkinPass)
            return result;

        if ((OverrideVanilla || usingDefaultSkin(player)) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player.getGameProfile());
            if (skin != null && skin.isSkinReady())
                return skin.getSkinType();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static boolean usingDefaultCape(AbstractClientPlayer player)
    {
        try
        {
            CapePass = true;
            return player.getLocationCape() == null;
        }
        finally
        {
            CapePass = false;
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean usingDefaultSkin(AbstractClientPlayer player)
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

    @SideOnly(Side.CLIENT)
    private static boolean SkinPass = false;
    @SideOnly(Side.CLIENT)
    private static boolean CapePass = false;

    @SideOnly(Side.CLIENT)
    public static ISkinProviderService skinService;
    @SideOnly(Side.CLIENT)
    public static ISkinProviderService capeService;

    @SideOnly(Side.CLIENT)
    public static boolean OverrideVanilla = false;

    @SideOnly(Side.CLIENT)
    private Map<Class<?>, Optional<Field[]>> allSubModelFields = new HashMap<Class<?>, Optional<Field[]>>();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleClientTicks(TickEvent.ClientTickEvent event)
    {
        if (skinService == null && capeService == null)
            return;

        if (event.phase == TickEvent.Phase.START)
        {
            World world = Minecraft.getMinecraft().world;
            if (world != null && world.playerEntities != null && !world.playerEntities.isEmpty())
            {
                for (Object obj : world.playerEntities)
                {
                    // This should keep skins/capes loaded.
                    if (obj instanceof AbstractClientPlayer)
                    {
                        if (skinService != null)
                            skinService.getSkin(((AbstractClientPlayer) obj).getGameProfile());
                        if (capeService != null)
                            capeService.getSkin(((AbstractClientPlayer) obj).getGameProfile());
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handlePlayerRender_Post(RenderPlayerEvent.Post event)
    {
        ModelPlayer model = event.getRenderer().getMainModel();
        model.textureWidth = 64;
        model.textureHeight = 64;
        setSubModelTextureSize_Main(model, 64, 64);
        setSubModelTextureSize_Cape(model, 64, 32);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handlePlayerRender_Pre(RenderPlayerEvent.Pre event)
    {
        EntityPlayer p = event.getEntityPlayer();
        if (p instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer player = (AbstractClientPlayer) p;
            ModelPlayer model = event.getRenderer().getMainModel();
            if ((OverrideVanilla || usingDefaultSkin(player)) && skinService != null)
            {
                ISkin skin = skinService.getSkin(player.getGameProfile());
                if (skin != null && skin.isSkinReady())
                {
                    if (skin instanceof SkinData)
                    {
                        BufferedImage image = ((SkinData) skin).getImage();
                        model.textureWidth = image.getWidth();
                        model.textureHeight = image.getHeight();
                        setSubModelTextureSize_Main(model, image.getWidth(), image.getHeight());
                    }
                }
            }
            if ((OverrideVanilla || usingDefaultCape(player)) && capeService != null)
            {
                ISkin cape = capeService.getSkin(player.getGameProfile());
                if (cape != null && cape.isSkinReady())
                {
                    if (cape instanceof SkinData)
                    {
                        BufferedImage image = ((SkinData) cape).getImage();
                        setSubModelTextureSize_Cape(model, image.getWidth(), image.getHeight());
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
            boolean useCrafatar = config.getBoolean("useCrafatar", Configuration.CATEGORY_CLIENT, true, "");
            boolean useCustomServer = config.getBoolean("useCustomServer", Configuration.CATEGORY_CLIENT, false, "");
            String hostCustomServer = config.getString("hostCustomServer", Configuration.CATEGORY_CLIENT, "http://example.com", "only http/https are supported, /skins/(uuid|username) and /capes/(uuid|username) will be queried for respective resources");
            if (config.hasChanged())
                config.save();

            skinService = SkinProviderAPI.createService();
            capeService = SkinProviderAPI.createService();

            skinService.register(new MojangCachedSkinProvider());
            skinService.register(new UserManagedSkinProvider());
            if (useCrafatar)
                skinService.register(new CrafatarCachedSkinProvider());
            if (useCustomServer)
                skinService.register(new CustomServerCachedSkinProvider(hostCustomServer));
            capeService.register(new MojangCachedCapeProvider());
            capeService.register(new UserManagedCapeProvider());
            if (useCrafatar)
                capeService.register(new CrafatarCachedCapeProvider());
            if (useCustomServer)
                capeService.register(new CustomServerCachedCapeProvider(hostCustomServer));

            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SideOnly(Side.CLIENT)
    private void setSubModelTextureSize(ModelBase model, int width, int height, Predicate<ModelRenderer> filter)
    {
        Class<?> clazz = model.getClass();
        if (!allSubModelFields.containsKey(clazz))
        {
            try
            {
                List<Field> fields = new ArrayList<Field>();
                do
                {
                    for (Field f : clazz.getDeclaredFields())
                    {
                        try
                        {
                            if (ModelRenderer.class.isAssignableFrom(f.getType()))
                            {
                                f.setAccessible(true);
                                fields.add(f);
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
                while (clazz != null);
                if (fields.isEmpty())
                    allSubModelFields.put(clazz, Optional.empty());
                else
                {
                    Field[] found = new Field[fields.size()];
                    fields.toArray(found);
                    allSubModelFields.put(clazz, Optional.of(found));
                }
            }
            catch (Exception e)
            {
                allSubModelFields.put(clazz, Optional.empty());
            }
        }

        allSubModelFields.get(clazz).ifPresent(fields -> {
            for (Field f : fields)
            {
                Object o = null;
                try
                {
                    o = f.get(model);
                }
                catch (Exception e)
                {
                }
                if (o != null)
                {
                    ModelRenderer m = (ModelRenderer) o;
                    if (filter.test(m))
                        m.setTextureSize(width, height);
                }
            }
        });
    }

    @SideOnly(Side.CLIENT)
    private void setSubModelTextureSize_Cape(ModelBase model, int width, int height)
    {
        setSubModelTextureSize(model, width, height, m -> m.textureWidth == m.textureHeight * 2);
    }

    @SideOnly(Side.CLIENT)
    private void setSubModelTextureSize_Main(ModelBase model, int width, int height)
    {
        setSubModelTextureSize(model, width, height, m -> m.textureWidth == m.textureHeight);
    }

}
