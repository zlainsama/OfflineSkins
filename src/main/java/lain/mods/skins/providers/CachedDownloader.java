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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lain.mods.skins.impl.Shared;

public class CachedDownloader
{

    public static CachedDownloader create()
    {
        return new CachedDownloader();
    }

    private int _cacheMinTTL = 600;
    private Map<String, String> _dataStore;
    private Predicate<Integer> _handler = code -> true;
    private Predicate<byte[]> _validator;
    private File _local;
    private int _maxTries = 5;
    private Proxy _proxy;
    private URL _remote;

    private CachedDownloader()
    {
    }

    private final byte[] doRead()
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
        while (tries++ < _maxTries)
        {
            try
            {
                boolean expired = _local.exists() && size == _local.length() ? System.currentTimeMillis() > expire : true;

                URLConnection conn = _proxy == null ? _remote.openConnection() : _remote.openConnection(_proxy);
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
                            try (FileOutputStream fos = new FileOutputStream(_local))
                            {
                                fos.getChannel().transferFrom(Channels.newChannel(conn.getInputStream()), 0, Long.MAX_VALUE);
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
                    try (FileOutputStream fos = new FileOutputStream(_local))
                    {
                        fos.getChannel().transferFrom(Channels.newChannel(conn.getInputStream()), 0, Long.MAX_VALUE);
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

                byte[] contents;
                if ((contents = Shared.blockyReadFile(_local, null, null)) != null && (_validator == null || _validator.test(contents)))
                    return contents;
            }
            catch (IOException e)
            {
            }
            if (tries < _maxTries && !Shared.sleep(1000L)) // wait 1 second before retry
                break; // interrupted
        }

        return null;
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
        return Shared.blockyCall(() -> {
            return doRead();
        }, null, null);
    }

    public CachedDownloader setCacheMinTTL(int cacheMinTTL)
    {
        _cacheMinTTL = cacheMinTTL;
        return this;
    }

    public CachedDownloader setDataStore(Map<String, String> dataStore)
    {
        _dataStore = dataStore;
        return this;
    }

    public CachedDownloader setErrorCodeHandler(Predicate<Integer> handler)
    {
        _handler = handler;
        return this;
    }

    public CachedDownloader setLocal(File local)
    {
        _local = local;
        return this;
    }

    public CachedDownloader setLocal(File dir, String filename)
    {
        _local = new File(dir, filename);
        return this;
    }

    public CachedDownloader setLocal(File dir, String format, Object... args)
    {
        _local = new File(dir, String.format(format, args));
        return this;
    }

    public CachedDownloader setMaxTries(int maxTries)
    {
        _maxTries = maxTries;
        return this;
    }

    public CachedDownloader setProxy(Proxy proxy)
    {
        _proxy = proxy;
        return this;
    }

    public CachedDownloader setRemote(String remote)
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

    public CachedDownloader setRemote(String format, Object... args)
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

    public CachedDownloader setRemote(URL remote)
    {
        _remote = remote;
        return this;
    }

    public CachedDownloader setValidator(Predicate<byte[]> validator)
    {
        _validator = validator;
        return this;
    }

}
