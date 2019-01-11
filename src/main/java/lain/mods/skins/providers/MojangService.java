package lain.mods.skins.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;

public class MojangService
{

    private static final LoadingCache<String, Optional<GameProfile>> cachedProfiles = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).build(new CacheLoader<String, Optional<GameProfile>>()
    {

        Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        @Override
        public Optional<GameProfile> load(String key) throws Exception
        {
            URL url = new URL(new StringBuilder().append("https://api.mojang.com/users/profiles/minecraft/").append(key).toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Minecraft.getMinecraft().getProxy());
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setUseCaches(false);
            InputStream in = null;
            try
            {
                in = conn.getInputStream();
            }
            catch (IOException e)
            {
                IOUtils.closeQuietly(in);
                in = conn.getErrorStream();
            }

            try
            {
                if (in != null)
                {
                    GameProfile parsed = gson.fromJson(IOUtils.toString(in, StandardCharsets.UTF_8), GameProfile.class);
                    if (parsed != null && parsed.getId() != null)
                        return Optional.of(Minecraft.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(parsed.getId(), parsed.getName()), false));
                }
            }
            catch (JsonSyntaxException ignored)
            {
            }
            catch (IOException ignored)
            {
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
            return Optional.absent();
        }

    });

    private static final GameProfile DUMMY = new GameProfile(UUID.fromString("fed3a6ca-d7de-11e5-b5d2-0a1d41d68578"), "[Dummy]");

    public static GameProfile getProfile(String username)
    {
        return getProfile(username, DUMMY);
    }

    public static GameProfile getProfile(String username, GameProfile defaultValue)
    {
        try
        {
            return cachedProfiles.get(username).or(defaultValue);
        }
        catch (ExecutionException ignored)
        {
            return defaultValue;
        }
    }

}
