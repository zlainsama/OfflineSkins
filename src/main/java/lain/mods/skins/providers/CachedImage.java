package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class CachedImage
{

    public static int CacheMinTTL = 600;

    public static void doCleanup(File dir)
    {
        List<CachedImage> images = new ArrayList<CachedImage>();
        Metadata m = new Metadata();
        for (File fMetadata : dir.listFiles(f -> f.getName().endsWith(".metadata")))
        {
            try
            {
                m.readFromFile(fMetadata);
            }
            catch (IOException e)
            {
                m.setEtag("");
                m.setValidtime(0);
            }

            if (!m.isValid())
                images.add(new CachedImage(new File(fMetadata.getParentFile(), fMetadata.getName().substring(0, fMetadata.getName().length() - 9))));
        }
        images.forEach(CachedImage::delete);
    }

    public static BufferedImage doRead(CachedImage image, URL url, Proxy proxy, int maxTries)
    {
        int count = 0;
        BufferedImage result = null;
        do
        {
            count++;
            result = image.readImage(url, proxy);
        }
        while (result == null && count < maxTries && image.getLastResponseCode() / 100 != 4);
        return result;
    }

    public static BufferedImage doRead(File file, String url, Proxy proxy, int maxTries)
    {
        try
        {
            return doRead(new CachedImage(file), new URL(url), proxy, maxTries);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    public static BufferedImage doRead(File dir, String filename, String url, Proxy proxy, int maxTries)
    {
        try
        {
            return doRead(new CachedImage(new File(dir, filename)), new URL(url), proxy, maxTries);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    public static BufferedImage doRead(File dir, String filename, URL url, Proxy proxy, int maxTries)
    {
        return doRead(new CachedImage(new File(dir, filename)), url, proxy, maxTries);
    }

    public static BufferedImage doRead(File file, URL url, Proxy proxy, int maxTries)
    {
        return doRead(new CachedImage(file), url, proxy, maxTries);
    }

    private static Map<String, String> parseField(String field)
    {
        return Pattern.compile(",").splitAsStream(field == null ? "" : field).map(String::trim).collect(HashMap::new, (m, s) -> {
            String[] as = s.split("=", 2);
            m.put(as[0], as.length == 2 ? as[1] : null);
        }, HashMap::putAll);
    }

    private File fImage;
    private File fMetadata;

    private int lastResponseCode = 0;

    public CachedImage(File file)
    {
        fImage = file;
        fMetadata = new File(file.getParentFile(), file.getName() + ".metadata");
    }

    public void delete()
    {
        fImage.delete();
        fMetadata.delete();
    }

    public int getLastResponseCode()
    {
        return lastResponseCode;
    }

    public BufferedImage readImage(URL url, Proxy proxy)
    {
        lastResponseCode = 0;

        Metadata m = new Metadata();
        try
        {
            m.readFromFile(fMetadata);
        }
        catch (IOException e)
        {
            m.setEtag("");
            m.setValidtime(0);
        }

        try
        {
            if (m.isValid() && fImage.exists())
            {
                BufferedImage image = ImageIO.read(fImage);
                if (image != null)
                    return image;
            }
        }
        catch (IOException e)
        {
            fImage.delete();
            fMetadata.delete();

            m.setEtag("");
            m.setValidtime(0);
        }

        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) url.openConnection(proxy);
        }
        catch (IOException e)
        {
            return null;
        }
        catch (ClassCastException e)
        {
            return null;
        }

        conn.setConnectTimeout(30000);
        conn.setReadTimeout(10000);
        if (m.hasEtag() && fImage.exists())
            conn.setRequestProperty("If-None-Match", m.getEtag());
        try
        {
            conn.connect();
            int code = conn.getResponseCode();
            lastResponseCode = code;
            int c = code / 100;
            if (c == 4)
                return null;
            else if (c == 2)
            {
                FileOutputStream s = null;
                try
                {
                    s = new FileOutputStream(fImage);
                    s.getChannel().transferFrom(Channels.newChannel(conn.getInputStream()), 0, Long.MAX_VALUE);
                }
                finally
                {
                    if (s != null)
                        s.close();
                }
                Map<String, String> cacheControl = parseField(conn.getHeaderField("Cache-Control"));
                if (!cacheControl.containsKey("no-cache"))
                {
                    m.setEtag(conn.getHeaderField("Etag"));
                    int age = 0;
                    try
                    {
                        if (cacheControl.containsKey("max-age"))
                            age = Integer.parseInt(cacheControl.get("max-age"));
                    }
                    catch (NumberFormatException e)
                    {
                        age = 0;
                    }
                    long cur = System.currentTimeMillis();
                    long validtime = Math.max(age > 0 ? (cur + (age * 1000)) : conn.getExpiration(), cur + (CacheMinTTL * 1000));
                    m.setValidtime(validtime);
                    m.writeToFile(fMetadata);
                }
                else
                {
                    fMetadata.delete();
                }
            }
        }
        catch (IOException e)
        {
            return null;
        }

        try
        {
            if (fImage.exists())
                return ImageIO.read(fImage);
            return null;
        }
        catch (IOException e)
        {
            fImage.delete();
            fMetadata.delete();
            return null;
        }
    }

}
