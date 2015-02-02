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
        imageWidth = 64;
        imageHeight = 64;

        BufferedImage localBufferedImage = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics localGraphics = localBufferedImage.getGraphics();
        localGraphics.drawImage(legacy, 0, 0, null);

        if (legacy.getHeight() == 32)
        {
            localGraphics.drawImage(localBufferedImage, 24, 48, 20, 52, 4, 16, 8, 20, null);
            localGraphics.drawImage(localBufferedImage, 28, 48, 24, 52, 8, 16, 12, 20, null);
            localGraphics.drawImage(localBufferedImage, 20, 52, 16, 64, 8, 20, 12, 32, null);
            localGraphics.drawImage(localBufferedImage, 24, 52, 20, 64, 4, 20, 8, 32, null);
            localGraphics.drawImage(localBufferedImage, 28, 52, 24, 64, 0, 20, 4, 32, null);
            localGraphics.drawImage(localBufferedImage, 32, 52, 28, 64, 12, 20, 16, 32, null);

            localGraphics.drawImage(localBufferedImage, 40, 48, 36, 52, 44, 16, 48, 20, null);
            localGraphics.drawImage(localBufferedImage, 44, 48, 40, 52, 48, 16, 52, 20, null);
            localGraphics.drawImage(localBufferedImage, 36, 52, 32, 64, 48, 20, 52, 32, null);
            localGraphics.drawImage(localBufferedImage, 40, 52, 36, 64, 44, 20, 48, 32, null);
            localGraphics.drawImage(localBufferedImage, 44, 52, 40, 64, 40, 20, 44, 32, null);
            localGraphics.drawImage(localBufferedImage, 48, 52, 44, 64, 52, 20, 56, 32, null);
        }

        localGraphics.dispose();

        imageData = ((DataBufferInt) localBufferedImage.getRaster().getDataBuffer()).getData();

        setAreaOpaque(0, 0, 32, 16);
        setAreaTransparent(32, 0, 64, 32);
        setAreaOpaque(0, 16, 64, 32);

        setAreaTransparent(0, 32, 16, 48);
        setAreaTransparent(16, 32, 40, 48);
        setAreaTransparent(40, 32, 56, 48);

        setAreaTransparent(0, 48, 16, 64);
        setAreaOpaque(16, 48, 48, 64);
        setAreaTransparent(48, 48, 64, 64);

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
