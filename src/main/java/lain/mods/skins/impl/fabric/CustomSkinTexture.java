package lain.mods.skins.impl.fabric;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class CustomSkinTexture extends ResourceTexture
{

    private WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(Identifier location, ByteBuffer data)
    {
        super(location);
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");
        _data = new WeakReference<ByteBuffer>(data);
    }

    public Identifier getLocation()
    {
        return location;
    }

    @Override
    public void load(ResourceManager resMan) throws IOException
    {
        NativeImage image = null;
        try
        {
            ByteBuffer buf;
            if ((buf = _data.get()) == null) // gc
                throw new FileNotFoundException(getLocation().toString());

            image = NativeImage.fromByteBuffer(buf.duplicate());

            synchronized (this)
            {
                bindTexture();
                TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, false);
            }
        }
        finally
        {
            if (image != null)
                image.close();
        }
    }

}
