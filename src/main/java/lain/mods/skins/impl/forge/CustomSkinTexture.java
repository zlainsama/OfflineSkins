package lain.mods.skins.impl.forge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomSkinTexture extends AbstractTexture
{

    private final ResourceLocation _location;
    private WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data)
    {
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        _location = location;
        _data = new WeakReference<>(data);
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

        try (NativeImage image = NativeImage.read(buf))
        {
            synchronized (this)
            {
                TextureUtil.allocateTextureImpl(getGlTextureId(), 0, image.getWidth(), image.getHeight());
                image.uploadTextureSub(0, 0, 0, false);
            }
        }
    }

}
