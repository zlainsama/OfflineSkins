package lain.mods.skins.impl.fabric;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class CustomSkinTexture extends ResourceTexture implements ISkinTexture {

    private final WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(Identifier location, ByteBuffer data) {
        super(location);
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        _data = new WeakReference<>(data);
    }

    @Override
    public ByteBuffer getData() {
        return _data.get();
    }

    public Identifier getLocation() {
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
        TextureUtil.prepareImage(getGlId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

}
