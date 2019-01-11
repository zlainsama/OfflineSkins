package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lain.mods.skins.LegacyConversion;
import lain.mods.skins.SkinData;
import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProvider;
import net.minecraft.client.Minecraft;

public class MojangCachedSkinProvider implements ISkinProvider
{

    private File _workDir;

    public MojangCachedSkinProvider()
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "mojang");
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
                if (Shared.isOfflineProfile(data.profile))
                    data.profile = MojangService.getProfile(data.profile.getName(), data.profile);

                BufferedImage image = null;
                UUID uuid = data.profile.getId();
                boolean slim = false;

                if (!Shared.isOfflineProfile(data.profile))
                {
                    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(data.profile);
                    if (textures.containsKey(MinecraftProfileTexture.Type.SKIN))
                    {
                        image = CachedImage.doRead(_workDir, uuid.toString(), textures.get(MinecraftProfileTexture.Type.SKIN).getUrl(), Minecraft.getMinecraft().getProxy(), 5);
                        if (image != null)
                        {
                            try
                            {
                                slim = "slim".equals(textures.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model"));
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }

                if (image != null)
                {
                    String type = SkinData.judgeSkinType(image);
                    if ("legacy".equals(type))
                        type = "default";
                    image = new LegacyConversion().convert(image);
                    if (slim)
                        type = "slim";
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
