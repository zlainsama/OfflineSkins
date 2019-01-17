package lain.mods.skins.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lain.mods.skins.impl.Shared;

public class CachedReader
{

    public static CachedReader create()
    {
        return new CachedReader();
    }

    private int _cacheMinTTL = 600;
    private Map<String, String> _dataStore;
    private Predicate<Integer> _handler = code -> true;
    private File _local;
    private int _maxTries = 5;
    private Proxy _proxy;
    private URL _remote;

    private CachedReader()
    {
    }

    private Map<String, String> parseField(String field)
    {
        return Pattern.compile(",").splitAsStream(field == null ? "" : field).map(String::trim).collect(HashMap::new, (m, s) -> {
            String[] as = s.split("=", 2);
            m.put(as[0], as.length == 2 ? as[1] : null);
        }, HashMap::putAll);
    }

    public byte[] read()
    {
        if (_local == null || _remote == null || _dataStore == null)
            return null;
        if (_local.exists() && (_local.isDirectory() || !_local.canRead() || !_local.canWrite()))
            return null;

        String key = Integer.toHexString(_local.hashCode());
        String[] metadata = _dataStore.getOrDefault(key, "0:0:").split(":", 3);

        long size = 0;
        long expire = 0;
        String etag = "";

        if (metadata.length == 3)
        {
            try
            {
                size = Long.parseLong(metadata[0]);
                expire = Long.parseLong(metadata[1]);
                etag = metadata[2];
            }
            catch (NumberFormatException e)
            {
                size = 0;
                expire = 0;
                etag = "";
            }
        }

        int tries = 0;

        URLConnection conn = null;
        while (tries++ < _maxTries)
        {
            try
            {
                boolean expired = _local.exists() && size == _local.length() ? System.currentTimeMillis() > expire : true;

                if (_proxy != null)
                    conn = _remote.openConnection(_proxy);
                else
                    conn = _remote.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(10000);
                if (!expired && !etag.isEmpty())
                    conn.setRequestProperty("If-None-Match", etag);
                conn.connect();

                if (conn instanceof HttpURLConnection)
                {
                    HttpURLConnection c = (HttpURLConnection) conn;
                    int code = c.getResponseCode();
                    switch (code / 100)
                    {
                        case 4:
                            return null;
                        case 2:
                            FileOutputStream fos = null;
                            try
                            {
                                fos = new FileOutputStream(_local);
                                fos.getChannel().transferFrom(Channels.newChannel(conn.getInputStream()), 0, Long.MAX_VALUE);
                            }
                            finally
                            {
                                Shared.closeQuietly(fos);
                            }
                            break;
                        default:
                            if (code != 304 && !_handler.test(code))
                                return null;
                            break;
                    }
                }
                else
                {
                    FileOutputStream fos = null;
                    try
                    {
                        fos = new FileOutputStream(_local);
                        fos.getChannel().transferFrom(Channels.newChannel(conn.getInputStream()), 0, Long.MAX_VALUE);
                    }
                    finally
                    {
                        Shared.closeQuietly(fos);
                    }
                }

                Map<String, String> cacheControl = parseField(conn.getHeaderField("Cache-Control"));
                if (!cacheControl.containsKey("no-cache"))
                {
                    etag = conn.getHeaderField("Etag");
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
                    expire = Math.max(age > 0 ? (System.currentTimeMillis() + (age * 1000)) : conn.getExpiration(), System.currentTimeMillis() + (_cacheMinTTL * 1000));
                    size = _local.length();
                    _dataStore.put(key, size + ":" + expire + ":" + etag);
                }
                else
                {
                    _dataStore.remove(key);
                }

                if (_local.exists() && !_local.isDirectory() && _local.canRead())
                    return Files.readAllBytes(_local.toPath());
            }
            catch (IOException e)
            {
            }
        }

        return null;
    }

    public CachedReader setCacheMinTTL(int cacheMinTTL)
    {
        _cacheMinTTL = cacheMinTTL;
        return this;
    }

    public CachedReader setDataStore(Map<String, String> dataStore)
    {
        _dataStore = dataStore;
        return this;
    }

    public CachedReader setErrorCodeHandler(Predicate<Integer> handler)
    {
        _handler = handler;
        return this;
    }

    public CachedReader setLocal(File local)
    {
        _local = local;
        return this;
    }

    public CachedReader setLocal(File dir, String filename)
    {
        _local = new File(dir, filename);
        return this;
    }

    public CachedReader setLocal(File dir, String format, Object... args)
    {
        _local = new File(dir, String.format(format, args));
        return this;
    }

    public CachedReader setMaxRetries(int maxTries)
    {
        _maxTries = maxTries;
        return this;
    }

    public CachedReader setProxy(Proxy proxy)
    {
        _proxy = proxy;
        return this;
    }

    public CachedReader setRemote(String remote)
    {
        try
        {
            _remote = new URL(remote);
        }
        catch (MalformedURLException e)
        {
            _remote = null;
        }
        return this;
    }

    public CachedReader setRemote(String format, Object... args)
    {
        try
        {
            _remote = new URL(String.format(format, args));
        }
        catch (MalformedURLException e)
        {
            _remote = null;
        }
        return this;
    }

    public CachedReader setRemote(URL remote)
    {
        _remote = remote;
        return this;
    }

}
