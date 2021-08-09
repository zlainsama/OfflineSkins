package lain.mods.skins.impl.forge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

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
    public void load(ResourceManager manager) throws IOException {
        final ByteBuffer buffer = getData();
        if (buffer != null) {
            final NativeImage image = NativeImage.read(buffer.duplicate());
            if (!RenderSystem.isOnRenderThreadOrInit())
                RenderSystem.recordRenderCall(() -> upload(image));
            else
                upload(image);
        }
    }

    private void upload(NativeImage image) {
        TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

}
