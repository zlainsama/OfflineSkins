package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.LegacyConversion;
import lain.mods.skins.SkinData;
import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProvider;
import net.minecraft.client.Minecraft;

public class CrafatarCachedSkinProvider implements ISkinProvider
{

    private File _workDir;

    public CrafatarCachedSkinProvider()
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "crafatar");
        if (!file2.exists())
            file2.mkdirs();
        prepareWorkDir(_workDir = new File(file2, "skins"));
    }

    @Override
    public ISkin getSkin(GameProfile profile)
    {
        final SkinData data = new SkinData();
        data.profile = profile;
        Shared.pool.execute(new Runnable()
        {

            @Override
            public void run()
            {
                BufferedImage image = null;
                UUID uuid = data.profile.getId();

                if (!Shared.isOfflineProfile(data.profile))
                    image = CachedImage.doRead(_workDir, uuid.toString(), String.format("https://crafatar.com/skins/%s", uuid), Minecraft.getMinecraft().getProxy(), 5);

                if (image != null)
                {
                    String type = SkinData.judgeSkinType(image);
                    if ("legacy".equals(type))
                        type = "default";
                    image = new LegacyConversion().convert(image);
                    data.put(image, type);
                }
            }

        });
        return data;
    }

    private void prepareWorkDir(File workDir)
    {
        if (!workDir.exists())
        {
            workDir.mkdirs();
        }
        else
        {
            // Legacy
            for (File f : workDir.listFiles(f -> f.getName().endsWith(".validtime")))
            {
                String n = f.getName().substring(0, f.getName().length() - 10);
                new File(f.getParentFile(), n).delete();
                new File(f.getParentFile(), n + ".etag").delete();
                new File(f.getParentFile(), n + ".validtime").delete();
            }

            CachedImage.doCleanup(workDir);
        }
    }

}
