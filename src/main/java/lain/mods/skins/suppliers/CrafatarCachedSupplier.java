package lain.mods.skins.suppliers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import lain.mods.skins.ImageSupplier;
import lain.mods.skins.LegacyConversion;
import lain.mods.skins.OfflineSkins;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import com.google.common.base.Strings;

public class CrafatarCachedSupplier implements ImageSupplier
{

    public CrafatarCachedSupplier()
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "cachedImages");
        if (!file1.exists())
            file1.mkdirs();
        File file2 = new File(file1, "crafatar");
        if (!file2.exists())
            file2.mkdirs();
        else
        {
            for (File f : file2.listFiles(new FileFilter()
            {

                @Override
                public boolean accept(File pathname)
                {
                    if (pathname.getName().toLowerCase().endsWith(".validtime"))
                        return true;
                    return false;
                }

            }))
            {
                String validtime = null;
                try
                {
                    validtime = Strings.emptyToNull(FileUtils.readFileToString(f, "UTF-8"));
                }
                catch (IOException e)
                {
                    validtime = null;
                }
                long t = -1;
                try
                {
                    if (validtime != null)
                        t = Long.parseLong(validtime);
                    else
                        t = -1;
                }
                catch (NumberFormatException e)
                {
                    t = -1;
                }
                if (System.currentTimeMillis() >= t)
                {
                    String name = f.getName().substring(0, f.getName().length() - 10);
                    File f1 = new File(f.getParentFile(), name);
                    File f2 = new File(f.getParentFile(), name + ".etag");
                    File f3 = f;
                    if (f1.exists())
                        f1.delete();
                    if (f2.exists())
                        f2.delete();
                    if (f3.exists())
                        f3.delete();
                }
            }
        }
    }

    @Override
    public BufferedImage loadImage(String name)
    {
        try
        {
            if (name.startsWith("skins/") && name.endsWith(".png"))
            {
                name = name.substring(6, name.length() - 4);
                if (name.startsWith("uuid/"))
                    name = name.substring(5);

                File file0 = new File(new File(Minecraft.getMinecraft().mcDataDir, "cachedImages"), "crafatar");
                File file1 = new File(file0, name);
                File file2 = new File(file0, name + ".etag");
                File file3 = new File(file0, name + ".validtime");

                String validtime = null;
                try
                {
                    validtime = Strings.emptyToNull(FileUtils.readFileToString(file3, "UTF-8"));
                }
                catch (IOException e)
                {
                    validtime = null;
                }
                long t = -1;
                try
                {
                    if (validtime != null)
                        t = Long.parseLong(validtime);
                    else
                        t = -1;
                }
                catch (NumberFormatException e)
                {
                    t = -1;
                }

                String etag = null;
                if (file1.exists() && file2.exists())
                {
                    try
                    {
                        etag = Strings.emptyToNull(FileUtils.readFileToString(file2, "UTF-8"));
                    }
                    catch (IOException e)
                    {
                        etag = null;
                    }
                }

                URL url = new URL("https://crafatar.com/skins/" + name);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(Minecraft.getMinecraft().getProxy());
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(10000);
                if (etag != null && System.currentTimeMillis() < t)
                    conn.setRequestProperty("If-None-Match", etag);
                conn.connect();
                if (conn.getResponseCode() / 100 == 2)
                {
                    etag = Strings.emptyToNull(conn.getHeaderField("Etag"));
                    t = conn.getExpiration();
                    try
                    {
                        FileUtils.copyInputStreamToFile(conn.getInputStream(), file1);
                        if (etag != null)
                            FileUtils.writeStringToFile(file2, etag, "UTF-8");
                        if (t > -1)
                            FileUtils.writeStringToFile(file3, Long.toString(t), "UTF-8");
                    }
                    catch (IOException e)
                    {
                        if (file1.exists())
                            file1.delete();
                        if (file2.exists())
                            file2.delete();
                        if (file3.exists())
                            file3.delete();
                    }
                }

                if (!file1.exists())
                    return null;
                BufferedImage result = ImageIO.read(file1);
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
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
