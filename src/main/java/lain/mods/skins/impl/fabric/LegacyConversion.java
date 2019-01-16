package lain.mods.skins.impl.fabric;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.function.Function;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SkinRemappingImageFilter;

public class LegacyConversion
{

    public static Function<ByteBuffer, ByteBuffer> createFilter()
    {
        SkinRemappingImageFilter filter = new SkinRemappingImageFilter();
        return data -> {
            ByteBuffer original = data;
            try
            {
                File tmp = null;
                NativeImage tmpI = null;
                NativeImage tmpO = null;
                try
                {
                    tmp = File.createTempFile("mcos", null);
                    tmpI = NativeImage.fromByteBuffer(data);
                    tmpO = filter.filterImage(tmpI);
                    filter.method_3238();
                    data = ByteBuffer.wrap(Files.readAllBytes(tmp.toPath()));
                }
                finally
                {
                    if (tmp != null)
                        tmp.delete();
                    if (tmpI != null)
                        tmpI.close();
                    if (tmpO != null)
                        tmpO.close();
                }
            }
            catch (IOException e)
            {
                data = original;
            }
            return data;
        };
    }

}
