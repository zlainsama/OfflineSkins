package lain.mods.skins.impl.fabric;

import java.nio.ByteBuffer;
import java.util.function.Function;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SkinRemappingImageFilter;

public class LegacyConversion
{

    public static Function<ByteBuffer, ByteBuffer> createFilter()
    {
        SkinRemappingImageFilter filter = new SkinRemappingImageFilter();
        return data -> {
            filter.filterImage(NativeImage.fromByteBuffer(data)).makePixelArray();
            return data;
        };
    }

}
