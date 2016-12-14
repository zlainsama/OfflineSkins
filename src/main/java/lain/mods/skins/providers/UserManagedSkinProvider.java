package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import lain.mods.skins.LegacyConversion;
import lain.mods.skins.SkinData;
import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProvider;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import com.mojang.authlib.GameProfile;

public class UserManagedSkinProvider implements ISkinProvider
{

    public UserManagedSkinProvider()
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "skins");
        if (!file2.exists())
            file2.mkdirs();
        File file3 = new File(file2, "uuid");
        if (!file3.exists())
            file3.mkdirs();
    }

    @Override
    public ISkin getSkin(GameProfile profile)
    {
        BufferedImage image = null;
        if (!Shared.isOfflineProfile(profile))
            image = readImage(String.format("skins/uuid/%s.png", profile.getId().toString().replaceAll("-", "")));
        if (image == null && !StringUtils.isBlank(profile.getName()))
            image = readImage(String.format("skins/%s.png", profile.getName()));
        if (image == null)
            return null;
        SkinData data = new SkinData();
        String type = SkinData.judgeSkinType(image);
        if ("legacy".equals(type))
            type = "default";
        image = new LegacyConversion().convert(image);
        data.put(image, type);
        return data;
    }

    private BufferedImage readImage(String name)
    {
        try
        {
            return ImageIO.read(new File(new File(Minecraft.getMinecraft().mcDataDir, "cachedImages"), name));
        }
        catch (Exception e)
        {
        }
        return null;
    }

}
