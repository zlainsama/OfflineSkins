package lain.mods.skins.impl.forge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

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
        return location;
    }

    @Override
    public void load(ResourceManager resMan) throws IOException {
        releaseId();

        ByteBuffer buf;
        if ((buf = _data.get()) == null) // gc
            throw new FileNotFoundException(getLocation().toString());

        try (NativeImage image = NativeImage.read(buf)) {
            synchronized (this) {
                TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, false);
            }
        }
    }

}
