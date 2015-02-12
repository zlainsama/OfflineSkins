package lain.mods.skins.suppliers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lain.mods.skins.ImageSupplier;
import lain.mods.skins.LegacyConversion;
import lain.mods.skins.OfflineSkins;
import net.minecraft.client.Minecraft;

public class UserManagedSupplier implements ImageSupplier
{

    public UserManagedSupplier()
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "skins");
        if (!file2.exists())
            file2.mkdirs();
        File file3 = new File(file1, "capes");
        if (!file3.exists())
            file3.mkdirs();
        File file4 = new File(file2, "uuid");
        if (!file4.exists())
            file4.mkdirs();
        File file5 = new File(file3, "uuid");
        if (!file5.exists())
            file5.mkdirs();
    }

    @Override
    public BufferedImage loadImage(String name)
    {
        try
        {
            BufferedImage result = ImageIO.read(new File(new File(Minecraft.getMinecraft().mcDataDir, "cachedImages"), name));
            if (result.getWidth() != 64 || (result.getHeight() != 64 && result.getHeight() != 32))
                return null;
            if (result.getHeight() == 32)
                result = new LegacyConversion().convert(result);
            if (((result.getRGB(55, 20) & 0xFF000000) >>> 24) == 0)
                OfflineSkins.imagesType.put(result, "slim");
            else
                OfflineSkins.imagesType.put(result, "default");
            return result;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
