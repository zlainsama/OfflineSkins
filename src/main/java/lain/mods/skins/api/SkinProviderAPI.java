package lain.mods.skins.api;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.api.interfaces.ISkinProviderService;

public class SkinProviderAPI
{

    /**
     * The service for skins.
     */
    public static final ISkinProviderService SKIN = create();
    /**
     * The service for capes.
     */
    public static final ISkinProviderService CAPE = create();

    /**
     * @return an empty ISkinProviderService with default implementation, a single ISkin object will be created during runtime with all available ISkin objects bundled in it, if a corresponding profile changes during the lifetime of an ISkin object, the ISkin object will be discarded and a new one will be created.
     */
    public static ISkinProviderService create()
    {
        return new ISkinProviderService()
        {

            private final ISkin DUMMY = new ISkin()
            {

                @Override
                public ByteBuffer getData()
                {
                    return null;
                }

                @Override
                public String getSkinType()
                {
                    return null;
                }

                @Override
                public boolean isDataReady()
                {
                    return false;
                }

                @Override
                public void onRemoval()
                {
                }

                @Override
                public boolean setRemovalListener(Consumer<ISkin> listener)
                {
                    return false;
                }

                @Override
                public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter)
                {
                    return false;
                }

            };

            private final LoadingCache<IPlayerProfile, ISkin> cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS).removalListener(new RemovalListener<IPlayerProfile, ISkin>()
            {

                @Override
                public void onRemoval(RemovalNotification<IPlayerProfile, ISkin> notification)
                {
                    ISkin skin = notification.getValue();
                    if (skin != null)
                        skin.onRemoval();
                }

            }).build(new CacheLoader<IPlayerProfile, ISkin>()
            {

                @Override
                public ISkin load(IPlayerProfile key) throws Exception
                {
                    key.setUpdateListener(profileChangeListener);

                    return new ISkin()
                    {

                        private final Collection<ISkin> skins = providers.stream().map(provider -> {
                            return provider.getSkin(key);
                        }).filter(skin -> {
                            return skin != null;
                        }).collect(Collectors.toCollection(ArrayList::new));

                        private Optional<ISkin> find()
                        {
                            return skins.stream().filter(ISkin::isDataReady).findFirst();
                        }

                        @Override
                        public ByteBuffer getData()
                        {
                            return find().map(ISkin::getData).orElse(null);
                        }

                        @Override
                        public String getSkinType()
                        {
                            return find().map(ISkin::getSkinType).orElse(null);
                        }

                        @Override
                        public boolean isDataReady()
                        {
                            return find().map(ISkin::isDataReady).orElse(false);
                        }

                        @Override
                        public void onRemoval()
                        {
                            for (ISkin skin : skins)
                                skin.onRemoval();
                        }

                        @Override
                        public boolean setRemovalListener(Consumer<ISkin> listener)
                        {
                            boolean any = false;
                            for (ISkin skin : skins)
                                if (skin.setRemovalListener(listener) && !any)
                                    any = true;
                            return any;
                        }

                        @Override
                        public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter)
                        {
                            boolean any = false;
                            for (ISkin skin : skins)
                                if (skin.setSkinFilter(filter) && !any)
                                    any = true;
                            return any;
                        }

                    };
                }

            });

            private final List<ISkinProvider> providers = new CopyOnWriteArrayList<>();
            private final Consumer<IPlayerProfile> profileChangeListener = profile -> {
                if (cache.getIfPresent(profile) != null)
                    cache.refresh(profile);
            };

            @Override
            public void clearProviders()
            {
                providers.clear();
                cache.invalidateAll();
            }

            @Override
            public ISkin getSkin(IPlayerProfile profile)
            {
                if (profile == null)
                    return DUMMY;
                return cache.getUnchecked(profile);
            }

            @Override
            public boolean registerProvider(ISkinProvider provider)
            {
                if (provider == null || provider == this)
                    return false;
                return providers.add(provider);
            }

        };
    }

}
