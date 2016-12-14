package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

public class Shared
{

    public static boolean isOfflineProfile(GameProfile profile)
    {
        if (profile == null || profile.getId() == null)
            return true;
        return !profileOnlineStatus.getUnchecked(profile);
    }

    protected static final ExecutorService pool = Executors.newCachedThreadPool();
    protected static final BufferedImage dummy = new BufferedImage(1, 1, 2);

    private static final LoadingCache<GameProfile, Boolean> profileOnlineStatus = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.MINUTES).build(new CacheLoader<GameProfile, Boolean>()
    {

        @Override
        public Boolean load(GameProfile key) throws Exception
        {
            return !UUID.nameUUIDFromBytes(("OfflinePlayer:" + key.getName()).getBytes(Charsets.UTF_8)).equals(key.getId());
        }

    });

}
