package lain.mods.skins.impl.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class CustomSkinTexture extends HttpTexture implements ISkinTexture {

    private final WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data) {
        super(null, null, location, false, null);
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

    public NativeImage getImage() throws IOException {
        ByteBuffer buffer = getData();
        if (buffer == null)
            throw new FileNotFoundException();
        return NativeImage.read(buffer);
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        NativeImage image = getImage();
        if (!RenderSystem.isOnRenderThreadOrInit())
            RenderSystem.recordRenderCall(() -> upload(image));
        else
            upload(image);
    }

    private void upload(NativeImage image) {
        TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

}
