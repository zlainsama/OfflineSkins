package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "OfflineSkins", useMetadata = true)
public class OfflineSkins
{

    public class Handler implements IWorldAccess, IResourceManagerReloadListener
    {
        @Override
        public void broadcastSound(int var1, int var2, int var3, int var4, int var5)
        {
        }

        @Override
        public void destroyBlockPartially(int var1, int var2, int var3, int var4, int var5)
        {
        }

        @Override
        public void markBlockForRenderUpdate(int var1, int var2, int var3)
        {
        }

        @Override
        public void markBlockForUpdate(int var1, int var2, int var3)
        {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6)
        {
        }

        @Override
        public void onEntityCreate(Entity var1)
        {
            loadSkin(var1, false);
        }

        @Override
        public void onEntityDestroy(Entity var1)
        {
        }

        @Override
        public void onResourceManagerReload(IResourceManager var1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null)
                for (Object obj : mc.theWorld.loadedEntityList)
                    loadSkin(obj, false);
        }

        @Override
        public void onStaticEntitiesChanged()
        {
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
        public void playAuxSFX(EntityPlayer var1, int var2, int var3, int var4, int var5, int var6)
        {
        }

        @Override
        public void playRecord(String var1, int var2, int var3, int var4)
        {
        }

        @Override
        public void playSound(String var1, double var2, double var4, double var6, float var8, float var9)
        {
        }

        @Override
        public void playSoundToNearExcept(EntityPlayer var1, String var2, double var3, double var5, double var7, float var9, float var10)
        {
        }

        public void setEnabled()
        {
            MinecraftForge.EVENT_BUS.register(this);
            Object obj = Minecraft.getMinecraft().getResourceManager();
            if (obj instanceof IReloadableResourceManager)
                ((IReloadableResourceManager) obj).registerReloadListener(this);
        }

        @Override
        public void spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12)
        {
        }
    }

    public class ImageCache
    {
        private Map<String, BufferedImage> map = Maps.newHashMap();
        public List<ImageSupplier> suppliers = Lists.newArrayList();

        public BufferedImage getCachedImage(String name)
        {
            BufferedImage image = map.get(name);
            if (image == null)
            {
                for (ImageSupplier supplier : suppliers)
                    if ((image = supplier.loadImage(name)) != null)
                        break;
                if (image != null)
                    map.put(name, image);
            }
            return image;
        }
    }

    public interface ImageSupplier
    {
        public BufferedImage loadImage(String name);
    }

    public Handler handler;
    public ImageCache cachedImages;

    public BufferedImage getCachedImage(String name)
    {
        return cachedImages.getCachedImage(name);
    }

    public BufferedImage getCachedImage(String format, Object... params)
    {
        return getCachedImage(String.format(format, params));
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
            handler = new Handler();
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        if (handler != null)
        {
            cachedImages = new ImageCache();
            cachedImages.suppliers.add(new ImageSupplier()
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
                    File file4 = new File(file1, "skins_uuid");
                    if (!file4.exists())
                        file4.mkdirs();
                    File file5 = new File(file1, "capes_uuid");
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
            handler.setEnabled();
        }
    }

    public void loadSkin(Object obj, boolean flag)
    {
        if (obj instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer player = (AbstractClientPlayer) obj;
            ThreadDownloadImageData skin = player.getTextureSkin();
            if (skin != null && (flag || !skin.isTextureUploaded()))
            {
                BufferedImage image = getCachedImage("skins_uuid/%s.png", player.getUniqueID().toString().replaceAll("-", ""));
                if (image != null)
                    TextureUtil.uploadTextureImage(skin.getGlTextureId(), image);
                else
                {
                    image = getCachedImage("skins/%s.png", player.getCommandSenderName());
                    if (image != null)
                        TextureUtil.uploadTextureImage(skin.getGlTextureId(), image);
                }
            }
            ThreadDownloadImageData cape = player.getTextureCape();
            if (cape != null && (flag || !cape.isTextureUploaded()))
            {
                BufferedImage image = getCachedImage("capes_uuid/%s.png", player.getUniqueID().toString().replaceAll("-", ""));
                if (image != null)
                    TextureUtil.uploadTextureImage(skin.getGlTextureId(), image);
                else
                {
                    image = getCachedImage("capes/%s.png", player.getCommandSenderName());
                    if (image != null)
                        TextureUtil.uploadTextureImage(skin.getGlTextureId(), image);
                }
            }
        }
    }

}
