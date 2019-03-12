package lain.mods.skins.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;
import javax.imageio.ImageIO;

public class LegacyConversion
{

    public static Function<ByteBuffer, ByteBuffer> createFilter()
    {
        return data -> {
            ByteBuffer original = data;
            try (InputStream in = SkinData.wrapByteBufferAsInputStream(data); ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                BufferedImage image = new LegacyConversion().convert(ImageIO.read(in));
                if (image != null)
                {
                    ImageIO.write(image, "png", baos);
                    baos.flush();
                    data = SkinData.toBuffer(baos.toByteArray());
                }
            }
            catch (Throwable t)
            {
                data = original;
            }
            return data;
        };
    }

    private int[] imageData;
    private int imageWidth;
    private int imageHeight;

    public BufferedImage convert(BufferedImage image)
    {
        if (image == null)
            return null;

        int r = Math.max(image.getWidth() / 64, 1);

        imageWidth = image.getWidth();
        imageHeight = imageWidth;

        BufferedImage i = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics g = i.getGraphics();
        g.drawImage(image, 0, 0, null);

        boolean legacy = image.getWidth() == image.getHeight() * 2;

        if (legacy)
        {
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0 * r, 32 * r, 64 * r, 32 * r);
            g.drawImage(i, 24 * r, 48 * r, 20 * r, 52 * r, 4 * r, 16 * r, 8 * r, 20 * r, null);
            g.drawImage(i, 28 * r, 48 * r, 24 * r, 52 * r, 8 * r, 16 * r, 12 * r, 20 * r, null);
            g.drawImage(i, 20 * r, 52 * r, 16 * r, 64 * r, 8 * r, 20 * r, 12 * r, 32 * r, null);
            g.drawImage(i, 24 * r, 52 * r, 20 * r, 64 * r, 4 * r, 20 * r, 8 * r, 32 * r, null);
            g.drawImage(i, 28 * r, 52 * r, 24 * r, 64 * r, 0 * r, 20 * r, 4 * r, 32 * r, null);
            g.drawImage(i, 32 * r, 52 * r, 28 * r, 64 * r, 12 * r, 20 * r, 16 * r, 32 * r, null);
            g.drawImage(i, 40 * r, 48 * r, 36 * r, 52 * r, 44 * r, 16 * r, 48 * r, 20 * r, null);
            g.drawImage(i, 44 * r, 48 * r, 40 * r, 52 * r, 48 * r, 16 * r, 52 * r, 20 * r, null);
            g.drawImage(i, 36 * r, 52 * r, 32 * r, 64 * r, 48 * r, 20 * r, 52 * r, 32 * r, null);
            g.drawImage(i, 40 * r, 52 * r, 36 * r, 64 * r, 44 * r, 20 * r, 48 * r, 32 * r, null);
            g.drawImage(i, 44 * r, 52 * r, 40 * r, 64 * r, 40 * r, 20 * r, 44 * r, 32 * r, null);
            g.drawImage(i, 48 * r, 52 * r, 44 * r, 64 * r, 52 * r, 20 * r, 56 * r, 32 * r, null);
        }

        g.dispose();

        imageData = ((DataBufferInt) i.getRaster().getDataBuffer()).getData();

        setAreaOpaque(0 * r, 0 * r, 32 * r, 16 * r);
        if (legacy)
            setAreaTransparent(32 * r, 0 * r, 64 * r, 32 * r);
        setAreaOpaque(0 * r, 16 * r, 64 * r, 32 * r);
        setAreaOpaque(16 * r, 48 * r, 48 * r, 64 * r);

        return i;
    }

    private void setAreaOpaque(int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
        {
            for (int j = y; j < height; ++j)
            {
                imageData[i + j * imageWidth] |= -16777216;
            }
        }
    }

    private void setAreaTransparent(int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
        {
            for (int j = y; j < height; ++j)
            {
                int k = imageData[i + j * imageWidth];

                if ((k >> 24 & 255) < 128)
                {
                    return;
                }
            }
        }

        for (int l = x; l < width; ++l)
        {
            for (int i1 = y; i1 < height; ++i1)
            {
                imageData[l + i1 * imageWidth] &= 16777215;
            }
        }
    }

}
