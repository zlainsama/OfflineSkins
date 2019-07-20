package lain.mods.skins.impl.forge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import com.mojang.blaze3d.platform.TextureUtil;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomSkinTexture extends Texture implements ISkinTexture
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

        try (NativeImage image = NativeImage.read(buf))
        {
            synchronized (this)
            {
                TextureUtil.prepareImage(getGlTextureId(), 0, image.getWidth(), image.getHeight());
                image.uploadTextureSub(0, 0, 0, false);
            }
        }
    }

}
