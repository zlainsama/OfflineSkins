package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.ISkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class SkinData implements ISkin
{

    class CachedTexture extends AbstractTexture
    {

        final ResourceLocation location;
        final SkinData skin;

        CachedTexture(ResourceLocation location, SkinData skin)
        {
            this.location = location;
            this.skin = skin;
        }

        @Override
        public void deleteGlTexture()
        {
            super.deleteGlTexture();
            SkinData.store.remove(location);
        }

        @Override
        public void loadTexture(IResourceManager arg0) throws IOException
        {
            deleteGlTexture();

            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), skin.image, false, false);
            SkinData.store.put(location, skin);
        }

    }

    private static Set<String> DefaultSkins = ImmutableSet.of("textures/entity/steve.png", "textures/entity/alex.png");
    private static Map<ResourceLocation, SkinData> store = new HashMap<ResourceLocation, SkinData>();

    public static SkinData getData(ResourceLocation location)
    {
        return SkinData.store.get(location);
    }

    public static boolean isDefaultSkin(ResourceLocation location)
    {
        if (location == null || !"minecraft".equals(location.getResourceDomain()))
            return false;
        return DefaultSkins.contains(location.getResourcePath());
    }

    public static String judgeSkinType(BufferedImage image)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        if (w == h * 2)
            return "legacy";
        if (w == h)
        {
            int r = Math.max(w / 64, 1);
            if (((image.getRGB(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                return "slim";
            return "default";
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

    public final BufferedImage getImage()
    {
        return this.image;
    }

    @Override
    public ResourceLocation getSkinLocation()
    {
        if (image != null && location != null)
        {
            if (texture == null || texture.skin.image != image)
            {
                if (texture != null)
                    texture.deleteGlTexture();
                Minecraft.getMinecraft().getTextureManager().loadTexture(location, texture = new CachedTexture(location, this));
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
