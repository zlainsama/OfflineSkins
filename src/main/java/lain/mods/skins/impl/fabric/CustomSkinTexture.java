package lain.mods.skins.impl.fabric;

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

    private volatile boolean ready = false;
    private WeakReference<ByteBuffer> data;

    public CustomSkinTexture(Identifier location, ByteBuffer data)
    {
        super(location);
        this.data = new WeakReference<ByteBuffer>(data);
    }

    public Identifier getLocation()
    {
        return location;
    }

    @Override
    public void load(ResourceManager resMan) throws IOException
    {
        if (!ready)
        {
            synchronized (this)
            {
                super.load(resMan);
                ready = true;
            }
        }

        NativeImage image = null;
        try
        {
            image = NativeImage.fromByteBuffer(data.get().duplicate());

            synchronized (this)
            {
                TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, false);
                ready = true;
            }
        }
        finally
        {
            if (image != null)
                image.close();
        }
    }

}
