package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import lain.mods.skins.api.ISkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import com.mojang.authlib.GameProfile;

public class SkinData implements ISkin
{

    class CachedTexture extends AbstractTexture
    {

        final BufferedImage image;

        CachedTexture(BufferedImage image)
        {
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager arg0) throws IOException
        {
            deleteGlTexture();

            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
        }

    }

    public static String judgeSkinType(BufferedImage image)
    {
        if (image.getWidth() == 64)
        {
            int height = image.getHeight();
            if (height == 32)
                return "legacy";
            else if (height == 64)
            {
                if (((image.getRGB(55, 20) & 0xFF000000) >>> 24) == 0)
                    return "slim";
                return "default";
            }
        }
        return "unknown";
    }

    public GameProfile profile;

    private String type;
    private BufferedImage image;
    private ResourceLocation location;

    private CachedTexture texture;

    public SkinData()
    {
        this(new ResourceLocation("offlineskins", String.format("textures/entity/generated/%s", UUID.randomUUID())));
    }

    public SkinData(ResourceLocation location)
    {
        this.location = location;
    }

    @Override
    public ResourceLocation getSkinLocation()
    {
        if (image != null && location != null)
        {
            if (texture == null || texture.image != image)
            {
                if (texture != null)
                    texture.deleteGlTexture();
                Minecraft.getMinecraft().getTextureManager().loadTexture(location, texture = new CachedTexture(image));
            }
        }
        return location;
    }

    @Override
    public String getSkinType()
    {
        return type;
    }

    @Override
    public boolean isSkinReady()
    {
        return image != null;
    }

    @Override
    public void onRemoval()
    {
        if (texture != null)
        {
            texture.deleteGlTexture();
            texture = null;
        }
    }

    public void put(BufferedImage image, String type)
    {
        if (image != null && type == null)
            type = judgeSkinType(image);

        this.type = type;
        this.image = image;
    }

}
