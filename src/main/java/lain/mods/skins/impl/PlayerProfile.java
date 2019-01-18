package lain.mods.skins.impl;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class PlayerProfile implements IPlayerProfile
{

    private static final LoadingCache<GameProfile, PlayerProfile> profiles = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<GameProfile, PlayerProfile>()
    {

        @Override
        public PlayerProfile load(GameProfile key) throws Exception
        {
            if (key == null || key == Shared.DUMMY)
                return new PlayerProfile(Shared.DUMMY);

            PlayerProfile profile = new PlayerProfile(key);
            if (Shared.isOfflinePlayer(key.getId(), key.getName()))
            {
                if (Shared.isBlank(key.getName()))
                    return new PlayerProfile(Shared.DUMMY);
                ListenableFuture<GameProfile> f1 = MojangService.getProfile(key.getName());
                if (f1.isDone())
                {
                    try
                    {
                        GameProfile resolved = f1.get();
                        if (resolved != Shared.DUMMY)
                        {
                            profile.set(resolved);
                            ListenableFuture<GameProfile> f2 = MojangService.fillProfile(resolved);
                            if (f2.isDone())
                            {
                                try
                                {
                                    GameProfile filled = f2.get();
                                    if (filled != resolved)
                                    {
                                        profile.set(filled);
                                    }
                                }
                                catch (Throwable t)
                                {
                                }
                            }
                            else if (!f2.isCancelled())
                            {
                                Futures.addCallback(f2, new FutureCallback<GameProfile>()
                                {

                                    @Override
                                    public void onFailure(Throwable t)
                                    {
                                    }

                                    @Override
                                    public void onSuccess(GameProfile filled)
                                    {
                                        if (filled != resolved)
                                        {
                                            profile.set(filled);
                                        }
                                    }

                                }, Shared.pool);
                            }
                        }
                    }
                    catch (Throwable t)
                    {
                    }
                }
                else if (!f1.isCancelled())
                {
                    Futures.addCallback(f1, new FutureCallback<GameProfile>()
                    {

                        @Override
                        public void onFailure(Throwable t)
                        {
                        }

                        @Override
                        public void onSuccess(GameProfile resolved)
                        {
                            if (resolved != Shared.DUMMY)
                            {
                                profile.set(resolved);
                                ListenableFuture<GameProfile> f2 = MojangService.fillProfile(resolved);
                                if (f2.isDone())
                                {
                                    try
                                    {
                                        GameProfile filled = f2.get();
                                        if (filled != resolved)
                                        {
                                            profile.set(filled);
                                        }
                                    }
                                    catch (Throwable t)
                                    {
                                    }
                                }
                                else if (!f2.isCancelled())
                                {
                                    Futures.addCallback(f2, new FutureCallback<GameProfile>()
                                    {

                                        @Override
                                        public void onFailure(Throwable t)
                                        {
                                        }

                                        @Override
                                        public void onSuccess(GameProfile filled)
                                        {
                                            if (filled != resolved)
                                            {
                                                profile.set(filled);
                                            }
                                        }

                                    }, Shared.pool);
                                }
                            }
                        }

                    }, Shared.pool);
                }
            }
            else if (key.getId() != null && key.getProperties().isEmpty())
            {
                ListenableFuture<GameProfile> f2 = MojangService.fillProfile(key);
                if (f2.isDone())
                {
                    try
                    {
                        GameProfile filled = f2.get();
                        if (filled != key)
                        {
                            profile.set(filled);
                        }
                    }
                    catch (Throwable t)
                    {
                    }
                }
                else if (!f2.isCancelled())
                {
                    Futures.addCallback(f2, new FutureCallback<GameProfile>()
                    {

                        @Override
                        public void onFailure(Throwable t)
                        {
                        }

                        @Override
                        public void onSuccess(GameProfile filled)
                        {
                            if (filled != key)
                            {
                                profile.set(filled);
                            }
                        }

                    }, Shared.pool);
                }
            }

            return profile;
        }

    });

    public static PlayerProfile wrapGameProfile(GameProfile profile)
    {
        return profiles.getUnchecked(profile);
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

    private synchronized void set(GameProfile profile)
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
