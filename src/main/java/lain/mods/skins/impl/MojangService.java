package lain.mods.skins.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import lain.mods.skins.impl.forge.MinecraftUtils;

public class MojangService
{

    private static final LoadingCache<String, Optional<GameProfile>> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).refreshAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<GameProfile>>()
    {

        private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        @Override
        public Optional<GameProfile> load(String key) throws Exception
        {
            try
            {
                GameProfile profile = makeRequest(String.format("https://api.mojang.com/users/profiles/minecraft/%s", key));
                if (profile != null && profile != Shared.DUMMY)
                    profile = MinecraftUtils.getSessionService().fillProfileProperties(profile, false);
                return Optional.ofNullable(profile);
            }
            catch (Throwable t)
            {
                return Optional.empty();
            }
        }

        private GameProfile makeRequest(String request) throws IOException
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(request).openConnection(MinecraftUtils.getProxy());
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setUseCaches(false);
            conn.connect();

            int code = conn.getResponseCode();
            if (code == 204)
                return Shared.DUMMY;
            else if (code / 100 == 2)
            {
                InputStream in = null;
                try
                {
                    in = conn.getInputStream();
                }
                catch (IOException e)
                {
                    Shared.closeQuietly(in);
                    in = conn.getErrorStream();
                }
                try
                {
                    if (in != null)
                    {
                        StringBuilder buf = new StringBuilder();
                        readLines(in, buf);
                        return gson.fromJson(buf.toString(), GameProfile.class);
                    }
                }
                catch (JsonSyntaxException e)
                {
                }
                catch (IOException e)
                {
                }
                finally
                {
                    Shared.closeQuietly(in);
                }
            }
            return null;
        }

        private void readLines(InputStream in, StringBuilder buf) throws IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            String newLine = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null)
            {
                if (buf.length() > 0)
                    buf.append(newLine);
                buf.append(line);
            }
        }

        @Override
        public ListenableFuture<Optional<GameProfile>> reload(String key, Optional<GameProfile> oldValue) throws Exception
        {
            if (oldValue.isPresent())
                return Futures.immediateFuture(oldValue);
            return super.reload(key, oldValue);
        }

    });

    /**
     * @param username the username to query about.
     * @return a ListenableFuture of a resolved online profile, otherwise {@link Shared#DUMMY DUMMY}.
     */
    public static ListenableFuture<GameProfile> getOnlineProfile(String username)
    {
        Optional<GameProfile> cachedResult;
        if ((cachedResult = cache.getIfPresent(username)) != null)
            return Futures.immediateFuture(cachedResult.orElse(Shared.DUMMY));
        return Shared.pool.submit(() -> {
            return cache.getUnchecked(username).orElse(Shared.DUMMY);
        });
    }

}
