package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

public class OfflineSkins extends DummyModContainer
{

    @SideOnly(Side.CLIENT)
    public static class OfflineTextureObject extends AbstractTexture
    {

        private BufferedImage image;

        public OfflineTextureObject(BufferedImage image)
        {
            this.image = image;
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
        if (!player.hasSkin())
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
                image = images.get(String.format("capes/%s.png", player.getName()));
            if (image == null)
                return null;
            TextureManager man = Minecraft.getMinecraft().getTextureManager();
            if (!(man.getTexture(locRes) instanceof OfflineTextureObject))
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
                image = images.get(String.format("skins/%s.png", player.getName()));
            if (image == null)
                return null;
            TextureManager man = Minecraft.getMinecraft().getTextureManager();
            if (!(man.getTexture(locRes) instanceof OfflineTextureObject))
                man.loadTexture(locRes, new OfflineTextureObject(image));
        }
        return locRes;
    }

    @SideOnly(Side.CLIENT)
    public static ImageCache images;

    public OfflineSkins()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "OfflineSkins";
        meta.name = "OfflineSkins";
        meta.version = "1.8-v1";
        meta.authorList = Arrays.asList("zlainsama");
        meta.description = "made it possible to cache your skins/capes for offline use";
        meta.credits = "";
        meta.url = "https://github.com/zlainsama/offlineskins";
        meta.updateUrl = "https://github.com/zlainsama/offlineskins/releases";
    }

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            images = new ImageCache();
            images.addSupplier(new ImageSupplier()
            {

                {
                    File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
                    if (!file1.exists())
                        file1.mkdirs();
                    File file2 = new File(file1, "skins");
                    if (!file2.exists())
                        file2.mkdirs();
                    File file3 = new File(file1, "capes");
                    if (!file3.exists())
                        file3.mkdirs();
                    File file4 = new File(file2, "uuid");
                    if (!file4.exists())
                        file4.mkdirs();
                    File file5 = new File(file3, "uuid");
                    if (!file5.exists())
                        file5.mkdirs();
                }

                @Override
                public BufferedImage loadImage(String name)
                {
                    try
                    {
                        return ImageIO.read(new File(new File(Minecraft.getMinecraft().mcDataDir, "cachedImages"), name));
                    }
                    catch (IOException ignored)
                    {
                        return null;
                    }
                }

            });
        }
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

}
