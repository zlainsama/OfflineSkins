package lain.mods.skins.impl.forge;

import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class CustomSkinTexture extends SimpleTexture implements ISkinTexture {

    private final WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data) {
        super(location);
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        _data = new WeakReference<>(data);
    }

    @Override
    public ByteBuffer getData() {
        return _data.get();
    }

    public ResourceLocation getLocation() {
        return textureLocation;
    }

    @Override
    public void loadTexture(IResourceManager resMan) throws IOException {
        deleteGlTexture();

        ByteBuffer buf;
        if ((buf = _data.get()) == null) // gc
            throw new FileNotFoundException(getLocation().toString());

        try (NativeImage image = NativeImage.read(buf)) {
            synchronized (this) {
                TextureUtil.prepareImage(getGlTextureId(), 0, image.getWidth(), image.getHeight());
                image.uploadTextureSub(0, 0, 0, false);
            }
        }
    }

}
