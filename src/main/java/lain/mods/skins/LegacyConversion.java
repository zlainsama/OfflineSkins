package lain.mods.skins;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

//code from net.minecraft.client.renderer.ImageBufferDownload
public class LegacyConversion
{
    private int[] imageData;
    private int imageWidth;
    private int imageHeight;

    public BufferedImage convert(BufferedImage legacy)
    {
        int r = legacy.getWidth() / 64;

        imageWidth = legacy.getWidth();
        imageHeight = imageWidth;

        BufferedImage localBufferedImage = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics localGraphics = localBufferedImage.getGraphics();
        localGraphics.drawImage(legacy, 0, 0, null);

        if (legacy.getWidth() == legacy.getHeight() * 2)
        {
            localGraphics.drawImage(localBufferedImage, 24 * r, 48 * r, 20 * r, 52 * r, 4 * r, 16 * r, 8 * r, 20 * r, null);
            localGraphics.drawImage(localBufferedImage, 28 * r, 48 * r, 24 * r, 52 * r, 8 * r, 16 * r, 12 * r, 20 * r, null);
            localGraphics.drawImage(localBufferedImage, 20 * r, 52 * r, 16 * r, 64 * r, 8 * r, 20 * r, 12 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 24 * r, 52 * r, 20 * r, 64 * r, 4 * r, 20 * r, 8 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 28 * r, 52 * r, 24 * r, 64 * r, 0 * r, 20 * r, 4 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 32 * r, 52 * r, 28 * r, 64 * r, 12 * r, 20 * r, 16 * r, 32 * r, null);

            localGraphics.drawImage(localBufferedImage, 40 * r, 48 * r, 36 * r, 52 * r, 44 * r, 16 * r, 48 * r, 20 * r, null);
            localGraphics.drawImage(localBufferedImage, 44 * r, 48 * r, 40 * r, 52 * r, 48 * r, 16 * r, 52 * r, 20 * r, null);
            localGraphics.drawImage(localBufferedImage, 36 * r, 52 * r, 32 * r, 64 * r, 48 * r, 20 * r, 52 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 40 * r, 52 * r, 36 * r, 64 * r, 44 * r, 20 * r, 48 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 44 * r, 52 * r, 40 * r, 64 * r, 40 * r, 20 * r, 44 * r, 32 * r, null);
            localGraphics.drawImage(localBufferedImage, 48 * r, 52 * r, 44 * r, 64 * r, 52 * r, 20 * r, 56 * r, 32 * r, null);
        }

        localGraphics.dispose();

        imageData = ((DataBufferInt) localBufferedImage.getRaster().getDataBuffer()).getData();

        setAreaOpaque(0 * r, 0 * r, 32 * r, 16 * r);
        setAreaTransparent(32 * r, 0 * r, 64 * r, 32 * r);
        setAreaOpaque(0 * r, 16 * r, 64 * r, 32 * r);

        setAreaTransparent(0 * r, 32 * r, 16 * r, 48 * r);
        setAreaTransparent(16 * r, 32 * r, 40 * r, 48 * r);
        setAreaTransparent(40 * r, 32 * r, 56 * r, 48 * r);

        setAreaTransparent(0 * r, 48 * r, 16 * r, 64 * r);
        setAreaOpaque(16 * r, 48 * r, 48 * r, 64 * r);
        setAreaTransparent(48 * r, 48 * r, 64 * r, 64 * r);

        return localBufferedImage;
    }

    private boolean hasTransparency(int p_hasTransparency_1_, int p_hasTransparency_2_, int p_hasTransparency_3_, int p_hasTransparency_4_)
    {
        for (int i = p_hasTransparency_1_; i < p_hasTransparency_3_; i++)
        {
            for (int j = p_hasTransparency_2_; j < p_hasTransparency_4_; j++)
            {
                int k = imageData[(i + j * imageWidth)];
                if ((k >> 24 & 0xFF) < 128)
                    return true;
            }
        }
        return false;
    }

    private void setAreaOpaque(int p_setAreaOpaque_1_, int p_setAreaOpaque_2_, int p_setAreaOpaque_3_, int p_setAreaOpaque_4_)
    {
        for (int i = p_setAreaOpaque_1_; i < p_setAreaOpaque_3_; i++)
            for (int j = p_setAreaOpaque_2_; j < p_setAreaOpaque_4_; j++)
                imageData[(i + j * imageWidth)] |= -16777216;
    }

    private void setAreaTransparent(int p_setAreaTransparent_1_, int p_setAreaTransparent_2_, int p_setAreaTransparent_3_, int p_setAreaTransparent_4_)
    {
        if (hasTransparency(p_setAreaTransparent_1_, p_setAreaTransparent_2_, p_setAreaTransparent_3_, p_setAreaTransparent_4_))
            return;

        for (int i = p_setAreaTransparent_1_; i < p_setAreaTransparent_3_; i++)
            for (int j = p_setAreaTransparent_2_; j < p_setAreaTransparent_4_; j++)
                imageData[(i + j * imageWidth)] &= 16777215;
    }

}
