package lain.mods.skins.impl.forge;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import lain.mods.skins.impl.SkinData;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomSkinTexture extends AbstractTexture
{

    private final ResourceLocation _location;
    private BufferedImage _image;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data)
    {
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        _location = location;

        try (InputStream in = SkinData.wrapByteBufferAsInputStream(data))
        {
            _image = ImageIO.read(in);
        }
        catch (Throwable t)
        {
            _image = null;
        }
    }

    public int getHeight()
    {
        BufferedImage image;
        if ((image = getImage()) == null)
            return 64; // default texture height
        return image.getHeight();
    }

    public BufferedImage getImage()
    {
        return _image;
    }

    public ResourceLocation getLocation()
    {
        return _location;
    }

    public int getWidth()
    {
        BufferedImage image;
        if ((image = getImage()) == null)
            return 64; // default texture width
        return image.getWidth();
    }

    @Override
    public void loadTexture(IResourceManager resMan) throws IOException
    {
        deleteGlTexture();

        if (_image == null)
            throw new FileNotFoundException(getLocation().toString());
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), _image, false, false);
    }

}
