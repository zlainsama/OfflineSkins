package lain.mods.skins.impl.forge;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import lain.mods.skins.api.interfaces.ISkinTexture;
import lain.mods.skins.impl.SkinData;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomSkinTexture extends AbstractTexture implements ISkinTexture
{

    private static BufferedImage loadImage(ByteBuffer buf)
    {
        try (InputStream in = SkinData.wrapByteBufferAsInputStream(buf))
        {
            return ImageIO.read(in);
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private final ResourceLocation _location;
    private WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data)
    {
        _location = location;
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");
        _data = new WeakReference<ByteBuffer>(data);
    }

    @Override
    public ByteBuffer getData()
    {
        return _data.get();
    }

    public ResourceLocation getLocation()
    {
        return _location;
    }

    @Override
    public void loadTexture(IResourceManager resMan) throws IOException
    {
        deleteGlTexture();

        ByteBuffer buf;
        if ((buf = _data.get()) == null) // gc
            throw new FileNotFoundException(getLocation().toString());
        BufferedImage image;
        if ((image = loadImage(buf)) == null)
            throw new FileNotFoundException(getLocation().toString());

        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
    }

}
