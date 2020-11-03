package lain.mods.skins.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import lain.mods.skins.impl.fabric.MinecraftUtils;

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

public class MojangService {

    private static final LoadingCache<GameProfile, Optional<GameProfile>> filledProfiles = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).refreshAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<GameProfile, Optional<GameProfile>>() {

        @Override
        public Optional<GameProfile> load(GameProfile key) throws Exception {
            if (key.getId() == null || key.getProperties() == null || key == Shared.DUMMY) // bad profile
                return Optional.empty();
            if (key.isComplete() && !key.getProperties().isEmpty()) // already filled
                return Optional.of(key);
            GameProfile filled = Shared.blockyCall(() -> {
                return MinecraftUtils.getSessionService().fillProfileProperties(key, false); // fill it
            }, key, null);
            if (filled == key) // failed
                return Optional.empty();
            if (!filled.isComplete() || filled.getProperties().isEmpty()) // partially filled, this won't happen in current implementation, it's here just in case.
                return Optional.empty();
            return Optional.of(filled); // cache it
        }

        @Override
        public ListenableFuture<Optional<GameProfile>> reload(GameProfile key, Optional<GameProfile> oldValue) throws Exception {
            if (oldValue.isPresent()) // good result, doesn't need refresh.
                return Futures.immediateFuture(oldValue);
            return Shared.submitTask(() -> {
                return load(key);
            });
        }

    });

    private static final LoadingCache<String, Optional<GameProfile>> resolvedProfiles = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).refreshAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<GameProfile>>() {

        private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        @Override
        public Optional<GameProfile> load(String key) throws Exception {
            if (Shared.isBlank(key)) // can't resolve this
                return Optional.of(Shared.DUMMY);
            return Optional.ofNullable(Shared.blockyCall(() -> {
                return makeRequest(String.format("https://api.mojang.com/users/profiles/minecraft/%s", key)); // request it
            }, null, null));
        }

        private GameProfile makeRequest(String request) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) new URL(request).openConnection(MinecraftUtils.getProxy());
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setUseCaches(false);
            conn.connect();

            int code = conn.getResponseCode();
            if (code == 204 || code == 404) // not found
                return Shared.DUMMY;
            else if (code / 100 == 2) {
                try (InputStream in = conn.getInputStream()) {
                    StringBuilder buf = new StringBuilder();
                    readLines(in, buf);
                    GameProfile constructed = gson.fromJson(buf.toString(), GameProfile.class);
                    if (!constructed.isComplete()) // why does the server return an incomplete profile? treat it as not found.
                        return Shared.DUMMY;
                    if (Shared.isOfflinePlayer(constructed.getId(), constructed.getName())) // why does the server return an offline profile? treat it as not found.
                        return Shared.DUMMY;
                    return new GameProfile(constructed.getId(), constructed.getName()); // reconstruct it because default JsonDeserializer doesn't construct a GameProfile properly, can't use GameProfileSerializer because it's a private class.
                }
            }
            return null;
        }

        private void readLines(InputStream in, StringBuilder buf) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            String newLine = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                if (buf.length() > 0)
                    buf.append(newLine);
                buf.append(line);
            }
        }

        @Override
        public ListenableFuture<Optional<GameProfile>> reload(String key, Optional<GameProfile> oldValue) throws Exception {
            if (oldValue.isPresent()) {
                if (oldValue.get() == Shared.DUMMY)
                    return Futures.immediateFuture(Optional.empty()); // effectively schedule a refresh in next reload.
                return Futures.immediateFuture(oldValue); // good result, doesn't need refresh.
            }
            return Shared.submitTask(() -> {
                return load(key);
            });
        }

    });

    /**
     * @param profile the profile to fill, requires an ID to actually fill.
     * @return a ListenableFuture of a filled profile, otherwise previous profile.
     */
    public static ListenableFuture<GameProfile> fillProfile(GameProfile profile) {
        if (profile == null)
            return Futures.immediateFailedFuture(new NullPointerException("profile must not be null"));
        Optional<GameProfile> cachedResult;
        if ((cachedResult = filledProfiles.getIfPresent(profile)) != null)
            return Futures.immediateFuture(cachedResult.orElse(profile));
        return Shared.submitTask(() -> {
            return filledProfiles.getUnchecked(profile).orElse(profile);
        });
    }

    /**
     * @param username the username to query about, requires non-blank to actually resolve.
     * @return a ListenableFuture of a resolved profile, otherwise {@link Shared#DUMMY DUMMY}.
     */
    public static ListenableFuture<GameProfile> getProfile(String username) {
        if (username == null)
            return Futures.immediateFailedFuture(new NullPointerException("username must not be null"));
        Optional<GameProfile> cachedResult;
        if ((cachedResult = resolvedProfiles.getIfPresent(username)) != null)
            return Futures.immediateFuture(cachedResult.orElse(Shared.DUMMY));
        return Shared.submitTask(() -> {
            return resolvedProfiles.getUnchecked(username).orElse(Shared.DUMMY);
        });
    }

}
