package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;
import lain.mods.skins.SkinData;
import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProvider;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import com.mojang.authlib.GameProfile;

public class CustomServerCachedCapeProvider implements ISkinProvider
{

    private File _workDir;
    private String _host;

    public CustomServerCachedCapeProvider(String host)
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "custom");
        if (!file2.exists())
            file2.mkdirs();
        prepareWorkDir(_workDir = new File(file2, "capes"));
        _host = host;
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
                String name = data.profile.getName();

                if (!Shared.isOfflineProfile(data.profile))
                    image = CachedImage.doRead(_workDir, uuid.toString(), String.format("%s/capes/%s", _host, uuid), Minecraft.getMinecraft().getProxy(), 5);
                if (image == null && !StringUtils.isBlank(name))
                    image = CachedImage.doRead(_workDir, name, String.format("%s/capes/%s", _host, name), Minecraft.getMinecraft().getProxy(), 5);

                if (image != null)
                {
                    data.put(image, "cape");
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
