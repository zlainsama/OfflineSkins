package lain.mods.skins.impl.fabric;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import lain.lib.Retries;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import net.minecraft.client.texture.NativeImage;

public class ImageUtils
{

    public static String judgeSkinType(byte[] data)
    {
        try (NativeImage image = NativeImage.fromInputStream(new ByteArrayInputStream(data)))
        {
            int w = image.getWidth();
            int h = image.getHeight();
            if (w == h * 2)
                return "default"; // it's actually "legacy", but there will always be a filter to convert them into "default".
            if (w == h)
            {
                int r = Math.max(w / 64, 1);
                if (((image.getPixelRGBA(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                    return "slim";
                return "default";
            }
            return "unknown";
        }
        catch (Throwable t)
        {
            return "unknown";
        }
    }

    public static ByteBuffer legacyFilter(ByteBuffer buffer)
    {
        try (NativeImage input = NativeImage.fromByteBuffer(buffer); NativeImage output = new NativeImage(input.getWidth(), input.getWidth(), true))
        {
            int r = Math.max(input.getWidth() / 64, 1);
            boolean f = input.getWidth() == input.getHeight() * 2;
            output.method_4317(input);
            if (f)
            {
                output.fillRGBA(0 * r, 32 * r, 64 * r, 32 * r, 0);
                output.method_4304(4 * r, 16 * r, 16 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.method_4304(8 * r, 16 * r, 16 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.method_4304(0 * r, 20 * r, 24 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(4 * r, 20 * r, 16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(8 * r, 20 * r, 8 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(12 * r, 20 * r, 16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(44 * r, 16 * r, -8 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.method_4304(48 * r, 16 * r, -8 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.method_4304(40 * r, 20 * r, 0 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(44 * r, 20 * r, -8 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(48 * r, 20 * r, -16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.method_4304(52 * r, 20 * r, -8 * r, 32 * r, 4 * r, 12 * r, true, false);
            }

            setAreaOpaque(output, 0 * r, 0 * r, 32 * r, 16 * r);
            if (f)
                setAreaTransparent(output, 32 * r, 0 * r, 64 * r, 32 * r);
            setAreaOpaque(output, 0 * r, 16 * r, 64 * r, 32 * r);
            setAreaOpaque(output, 16 * r, 48 * r, 48 * r, 64 * r);

            File tmp = null;
            try
            {
                output.writeFile(tmp = Files.createTempFile(null, null).toFile());
                return SkinData.toBuffer(Shared.blockyReadFile(tmp, null, Retries::rethrow));
            }
            finally
            {
                if (tmp != null && tmp.exists() && !tmp.delete())
                    tmp.deleteOnExit();
            }
        }
        catch (Throwable t)
        {
            return buffer;
        }
    }

    private static void setAreaOpaque(NativeImage image, int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
            for (int j = y; j < height; ++j)
                image.setPixelRGBA(i, j, image.getPixelRGBA(i, j) | -16777216);
    }

    private static void setAreaTransparent(NativeImage image, int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
            for (int j = y; j < height; ++j)
                if ((image.getPixelRGBA(i, j) >> 24 & 255) < 128)
                    return;

        for (int l = x; l < width; ++l)
            for (int i1 = y; i1 < height; ++i1)
                image.setPixelRGBA(l, i1, image.getPixelRGBA(l, i1) & 16777215);
    }

    public static boolean validateData(byte[] data)
    {
        try (NativeImage image = NativeImage.fromInputStream(new ByteArrayInputStream(data)))
        {
            return image != null;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

}
