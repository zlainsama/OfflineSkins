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

    public BufferedImage convert(BufferedImage newtype)
    {
        imageWidth = 64;
        imageHeight = 32;

        BufferedImage localBufferedImage = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics localGraphics = localBufferedImage.getGraphics();
        localGraphics.drawImage(newtype, 0, 0, null);
        localGraphics.dispose();

        imageData = ((DataBufferInt) localBufferedImage.getRaster().getDataBuffer()).getData();

        setAreaOpaque(0, 0, 32, 16);
        setAreaTransparent(32, 0, 64, 32);
        setAreaOpaque(0, 16, 64, 32);

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
