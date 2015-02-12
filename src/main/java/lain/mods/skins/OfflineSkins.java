package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import lain.mods.skins.suppliers.CrafatarCachedSupplier;
import lain.mods.skins.suppliers.UserManagedSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.config.Configuration;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OfflineSkins extends DummyModContainer
{

    @SideOnly(Side.CLIENT)
    public static class OfflineTextureObject extends AbstractTexture
    {

        private final BufferedImage image;

        public OfflineTextureObject(BufferedImage image)
        {
            this.image = image;
        }

        public BufferedImage getImage()
        {
            return image;
        }

        @Override
        public void loadTexture(IResourceManager arg0)
        {
            deleteGlTexture();

            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
        }

    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getFakeLocation(AbstractClientPlayer player, MinecraftProfileTexture.Type type)
    {
        switch (type)
        {
            case CAPE:
                return new ResourceLocation("offlineskins", String.format("capes/%s", player.getUniqueID()));
            case SKIN:
                return new ResourceLocation("offlineskins", String.format("skins/%s", player.getUniqueID()));
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (result == null)
            result = getOfflineCape(player);
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (!player.func_152123_o())
        {
            ResourceLocation tmp = getOfflineSkin(player);
            if (tmp != null)
                result = tmp;
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getOfflineCape(AbstractClientPlayer player)
    {
        ResourceLocation locRes = getFakeLocation(player, MinecraftProfileTexture.Type.CAPE);
        if (locRes != null)
        {
            BufferedImage image = images.get(String.format("capes/uuid/%s.png", player.getUniqueID().toString().replaceAll("-", "")));
            if (image == null)
                image = images.get(String.format("capes/%s.png", StringUtils.stripControlCodes(player.getCommandSenderName())));
            if (image == null)
                return null;
            TextureManager man = Minecraft.getMinecraft().getTextureManager();
            if (!(man.getTexture(locRes) instanceof OfflineTextureObject) || ((OfflineTextureObject) man.getTexture(locRes)).getImage() != image)
                man.loadTexture(locRes, new OfflineTextureObject(image));
        }
        return locRes;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getOfflineSkin(AbstractClientPlayer player)
    {
        ResourceLocation locRes = getFakeLocation(player, MinecraftProfileTexture.Type.SKIN);
        if (locRes != null)
        {
            BufferedImage image = images.get(String.format("skins/uuid/%s.png", player.getUniqueID().toString().replaceAll("-", "")));
            if (image == null)
                image = images.get(String.format("skins/%s.png", StringUtils.stripControlCodes(player.getCommandSenderName())));
            if (image == null)
                return null;
            TextureManager man = Minecraft.getMinecraft().getTextureManager();
            if (!(man.getTexture(locRes) instanceof OfflineTextureObject) || ((OfflineTextureObject) man.getTexture(locRes)).getImage() != image)
                man.loadTexture(locRes, new OfflineTextureObject(image));
        }
        return locRes;
    }

    @SideOnly(Side.CLIENT)
    public static final ImageCache images = new ImageCache();

    public OfflineSkins()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "OfflineSkins";
        meta.name = "OfflineSkins";
        meta.version = "1.7.10-v5";
        meta.authorList = Arrays.asList("zlainsama");
        meta.description = "made it possible to cache your skins/capes for offline use";
        meta.credits = "";
        meta.url = "https://github.com/zlainsama/offlineskins";
        meta.updateUrl = "";
    }

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            images.addSupplier(new UserManagedSupplier());
            if (config.get(Configuration.CATEGORY_GENERAL, "useCrafatar", true).getBoolean(true))
                images.addSupplier(new CrafatarCachedSupplier());
            if (config.hasChanged())
                config.save();
        }
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

}
