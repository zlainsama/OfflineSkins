package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

@Mod(modid = "OfflineSkins", useMetadata = true)
public class OfflineSkins
{

    @SideOnly(Side.CLIENT)
    public static class Handler implements IWorldAccess, IResourceManagerReloadListener
    {

        @Override
        public void broadcastSound(int arg0, BlockPos arg1, int arg2)
        {
        }

        @Override
        public void markBlockForUpdate(BlockPos arg0)
        {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)
        {
        }

        @Override
        public void notifyLightSet(BlockPos arg0)
        {
        }

        @Override
        public void onEntityAdded(Entity ent)
        {
            if (ent instanceof AbstractClientPlayer)
            {
                OfflineSkins.instance.getOfflineSkin((AbstractClientPlayer) ent, true);
                OfflineSkins.instance.getOfflineCape((AbstractClientPlayer) ent, true);
            }
        }

        @Override
        public void onEntityRemoved(Entity arg0)
        {
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onResourceManagerReload(IResourceManager arg0)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null)
            {
                for (Entity ent : (List<Entity>) mc.theWorld.playerEntities)
                {
                    if (ent instanceof AbstractClientPlayer)
                    {
                        OfflineSkins.instance.getOfflineSkin((AbstractClientPlayer) ent, true);
                        OfflineSkins.instance.getOfflineCape((AbstractClientPlayer) ent, true);
                    }
                }
            }
        }

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event)
        {
            event.world.addWorldAccess(this);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event)
        {
            event.world.removeWorldAccess(this);
        }

        @Override
        public void playAusSFX(EntityPlayer arg0, int arg1, BlockPos arg2, int arg3)
        {
        }

        @Override
        public void playRecord(String arg0, BlockPos arg1)
        {
        }

        @Override
        public void playSound(String arg0, double arg1, double arg2, double arg3, float arg4, float arg5)
        {
        }

        @Override
        public void playSoundToNearExcept(EntityPlayer arg0, String arg1, double arg2, double arg3, double arg4, float arg5, float arg6)
        {
        }

        @Override
        public void sendBlockBreakProgress(int arg0, BlockPos arg1, int arg2)
        {
        }

        public void setup()
        {
            MinecraftForge.EVENT_BUS.register(this);
            Object obj = Minecraft.getMinecraft().getResourceManager();
            if (obj instanceof IReloadableResourceManager)
                ((IReloadableResourceManager) obj).registerReloadListener(this);
        }

        @Override
        public void spawnParticle(int arg0, boolean arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, int... arg8)
        {
        }

    }

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
            result = OfflineSkins.instance.getOfflineCape(player, false);
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (!player.hasSkin())
            result = OfflineSkins.instance.getOfflineSkin(player, false);
        return result;
    }

    @SideOnly(Side.CLIENT)
    public ImageCache images;

    @Mod.Instance("OfflineSkins")
    public static OfflineSkins instance;

    @SideOnly(Side.CLIENT)
    public ResourceLocation getOfflineCape(AbstractClientPlayer player, boolean load)
    {
        ResourceLocation locRes = getFakeLocation(player, MinecraftProfileTexture.Type.CAPE);
        if (locRes != null)
        {
            BufferedImage image = images.get(String.format("capes/uuid/%s.png", player.getUniqueID().toString().replaceAll("-", "")), load);
            if (image == null)
                image = images.get(String.format("capes/%s.png", player.getName()), load);
            if (image != null)
                Minecraft.getMinecraft().getTextureManager().loadTexture(locRes, new OfflineTextureObject(image));
        }
        return locRes;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getOfflineSkin(AbstractClientPlayer player, boolean load)
    {
        ResourceLocation locRes = getFakeLocation(player, MinecraftProfileTexture.Type.SKIN);
        if (locRes != null)
        {
            BufferedImage image = images.get(String.format("skins/uuid/%s.png", player.getUniqueID().toString().replaceAll("-", "")), load);
            if (image == null)
                image = images.get(String.format("skins/%s.png", player.getName()), load);
            if (image != null)
                Minecraft.getMinecraft().getTextureManager().loadTexture(locRes, new OfflineTextureObject(image));
        }
        return locRes;
    }

    @Mod.EventHandler
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
            new Handler().setup();
        }
    }

}
