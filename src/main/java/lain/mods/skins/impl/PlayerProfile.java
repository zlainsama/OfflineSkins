package lain.mods.skins.impl;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class PlayerProfile implements IPlayerProfile
{

    private static final LoadingCache<GameProfile, PlayerProfile> profiles = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<GameProfile, PlayerProfile>()
    {

        @Override
        public PlayerProfile load(GameProfile key) throws Exception
        {
            return new PlayerProfile(key);
        }

    });

    public static PlayerProfile wrapGameProfile(GameProfile profile)
    {
        PlayerProfile p = profiles.getUnchecked(profile);

        if (Shared.isOfflinePlayerProfile(p))
        {
            ListenableFuture<GameProfile> future = MojangService.getOnlineProfile(p.getPlayerName());
            if (future.isDone())
            {
                try
                {
                    if (!future.isCancelled())
                    {
                        GameProfile resolved = future.get();
                        if (resolved != null && resolved != Shared.DUMMY && !Shared.isOfflinePlayerProfile(profiles.getUnchecked(resolved)))
                            p.set(resolved);
                    }
                }
                catch (Throwable ignored)
                {
                }
            }
        }

        return p;
    }

    private WeakReference<GameProfile> _profile;
    private final Collection<Consumer<IPlayerProfile>> _listeners = new CopyOnWriteArrayList<>();

    private PlayerProfile(GameProfile profile)
    {
        set(profile);
    }

    @Override
    public boolean equals(Object o)
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return false;
        if (o instanceof PlayerProfile)
            return p.equals(((PlayerProfile) o)._profile.get());
        return false;
    }

    @Override
    public Object getOriginal()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return Shared.DUMMY;
        return p;
    }

    @Override
    public UUID getPlayerID()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return Shared.DUMMY.getId();
        return p.getId();
    }

    @Override
    public String getPlayerName()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return Shared.DUMMY.getName();
        return p.getName();
    }

    @Override
    public int hashCode()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return 0;
        return p.hashCode();
    }

    private void set(GameProfile profile)
    {
        if (profile == null)
            throw new IllegalArgumentException("profile must not be null");
        _profile = new WeakReference<GameProfile>(profile);
        for (Consumer<IPlayerProfile> l : _listeners)
            l.accept(this);
    }

    @Override
    public boolean setUpdateListener(Consumer<IPlayerProfile> listener)
    {
        if (listener == null || _listeners.contains(listener))
            return false;
        return _listeners.add(listener);
    }

}
