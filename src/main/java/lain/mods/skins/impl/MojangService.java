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
import lain.mods.skins.impl.fabric.MinecraftUtils;

public class MojangService
{

    private static final LoadingCache<GameProfile, Optional<GameProfile>> filledProfiles = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).refreshAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<GameProfile, Optional<GameProfile>>()
    {

        @Override
        public Optional<GameProfile> load(GameProfile key) throws Exception
        {
            if (key == null || key.getId() == null)
                throw new IllegalArgumentException("bad profile");
            if (key.isComplete() && !key.getProperties().isEmpty())
                return Optional.of(key);
            GameProfile filled = MinecraftUtils.getSessionService().fillProfileProperties(key, false);
            if (key == filled)
                return Optional.empty();
            return Optional.of(filled);
        }

        @Override
        public ListenableFuture<Optional<GameProfile>> reload(GameProfile key, Optional<GameProfile> oldValue) throws Exception
        {
            if (oldValue.isPresent())
                return Futures.immediateFuture(oldValue);
            return Shared.pool.submit(() -> {
                return load(key);
            });
        }

    });

    private static final LoadingCache<String, Optional<GameProfile>> resolvedProfiles = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).refreshAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<GameProfile>>()
    {

        private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        @Override
        public Optional<GameProfile> load(String key) throws Exception
        {
            try
            {
                return Optional.ofNullable(makeRequest(String.format("https://api.mojang.com/users/profiles/minecraft/%s", key)));
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
                        GameProfile constructed = gson.fromJson(buf.toString(), GameProfile.class);
                        return new GameProfile(constructed.getId(), constructed.getName()); // reconstruct it because default JsonDeserializer doesn't construct a GameProfile properly, can't use GameProfileSerializer because it's a private class.
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
            {
                if (oldValue.get() == Shared.DUMMY)
                    return Futures.immediateFuture(Optional.empty());
                return Futures.immediateFuture(oldValue);
            }
            return Shared.pool.submit(() -> {
                return load(key);
            });
        }

    });

    /**
     * @param profile the profile to fill, requires an ID.
     * @return a ListenableFuture of a filled profile, otherwise previous profile.
     */
    public static ListenableFuture<GameProfile> fillProfile(GameProfile profile)
    {
        if (profile == null || profile.getId() == null)
            return Futures.immediateFuture(profile);
        Optional<GameProfile> cachedResult;
        if ((cachedResult = filledProfiles.getIfPresent(profile)) != null)
            return Futures.immediateFuture(cachedResult.orElse(profile));
        return Shared.pool.submit(() -> {
            return filledProfiles.getUnchecked(profile).orElse(profile);
        });
    }

    /**
     * @param username the username to query about.
     * @return a ListenableFuture of a resolved profile without any properties, otherwise {@link Shared#DUMMY DUMMY}.
     */
    public static ListenableFuture<GameProfile> getProfile(String username)
    {
        if (Shared.isBlank(username))
            return Futures.immediateFuture(Shared.DUMMY);
        Optional<GameProfile> cachedResult;
        if ((cachedResult = resolvedProfiles.getIfPresent(username)) != null)
            return Futures.immediateFuture(cachedResult.orElse(Shared.DUMMY));
        return Shared.pool.submit(() -> {
            return resolvedProfiles.getUnchecked(username).orElse(Shared.DUMMY);
        });
    }

}
