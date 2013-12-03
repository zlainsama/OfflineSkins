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
import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "OfflineSkins", useMetadata = true)
public class OfflineSkins
{

    public class Handler implements IWorldAccess, ResourceManagerReloadListener
    {
        @Override
        public void broadcastSound(int i, int j, int k, int l, int i1)
        {
        }

        @Override
        public void destroyBlockPartially(int i, int j, int k, int l, int i1)
        {
        }

        @Override
        public void markBlockForRenderUpdate(int i, int j, int k)
        {
        }

        @Override
        public void markBlockForUpdate(int i, int j, int k)
        {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int i, int j, int k, int l, int i1, int j1)
        {
        }

        @Override
        public void onEntityCreate(Entity entity)
        {
            loadSkin(entity, false);
        }

        @Override
        public void onEntityDestroy(Entity entity)
        {
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourcemanager)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null)
                for (Object obj : mc.theWorld.loadedEntityList)
                    loadSkin(obj, false);
        }

        @ForgeSubscribe
        public void onWorldLoad(WorldEvent.Load event)
        {
            event.world.addWorldAccess(this);
        }

        @ForgeSubscribe
        public void onWorldUnload(WorldEvent.Unload event)
        {
            event.world.removeWorldAccess(this);
        }

        @Override
        public void playAuxSFX(EntityPlayer entityplayer, int i, int j, int k, int l, int i1)
        {
        }

        @Override
        public void playRecord(String s, int i, int j, int k)
        {
        }

        @Override
        public void playSound(String s, double d0, double d1, double d2, float f, float f1)
        {
        }

        @Override
        public void playSoundToNearExcept(EntityPlayer entityplayer, String s, double d0, double d1, double d2, float f, float f1)
        {
        }

        public void setEnabled()
        {
            MinecraftForge.EVENT_BUS.register(this);
            Object obj = Minecraft.getMinecraft().getResourceManager();
            if (obj instanceof ReloadableResourceManager)
                ((ReloadableResourceManager) obj).registerReloadListener(this);
        }

        @Override
        public void spawnParticle(String s, double d0, double d1, double d2, double d3, double d4, double d5)
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
                @Override
                public BufferedImage loadImage(String name)
                {
                    ResourceManager resman = Minecraft.getMinecraft().getResourceManager();
                    ResourceLocation location = new ResourceLocation(name);
                    try
                    {
                        return ImageIO.read(resman.getResource(location).getInputStream());
                    }
                    catch (IOException ignored)
                    {
                        return null;
                    }
                }
            });
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
                BufferedImage image = cachedImages.getCachedImage(String.format("skins/%s.png", StringUtils.stripControlCodes(player.username)));
                if (image != null)
                    TextureUtil.uploadTextureImage(skin.getGlTextureId(), image);
            }
            ThreadDownloadImageData cape = player.getTextureCape();
            if (cape != null && (flag || !cape.isTextureUploaded()))
            {
                BufferedImage image = cachedImages.getCachedImage(String.format("capes/%s.png", StringUtils.stripControlCodes(player.username)));
                if (image != null)
                    TextureUtil.uploadTextureImage(cape.getGlTextureId(), image);
            }
        }
    }

}
