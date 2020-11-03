package lain.mods.skins.api;

import com.google.common.cache.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lain.lib.SharedPool;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.api.interfaces.ISkinProviderService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SkinProviderAPI {

    public static final ISkin DUMMY = new ISkin() {

        @Override
        public ByteBuffer getData() {
            return null;
        }

        @Override
        public String getSkinType() {
            return null;
        }

        @Override
        public boolean isDataReady() {
            return false;
        }

        @Override
        public void onRemoval() {
        }

        @Override
        public boolean setRemovalListener(Consumer<ISkin> listener) {
            return false;
        }

        @Override
        public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter) {
            return false;
        }

    };

    /**
     * The service for skins.
     */
    public static final ISkinProviderService SKIN = create();
    /**
     * The service for capes.
     */
    public static final ISkinProviderService CAPE = create();

    /**
     * @return an empty ISkinProviderService with default implementation. <br>
     * a SkinBundle will be created with all available ISkin objects for that IPlayerProfile. <br>
     * if the profile got updated during the lifetime of a SkinBundle, new ISkin objects will be gathered and a thread will be used to monitor those objects to wait their isDataReady for up to 10 seconds before updating the SkinBundle.
     */
    public static ISkinProviderService create() {
        return new ISkinProviderService() {

            private final LoadingCache<SkinBundle, AtomicReference<Object>> reloading;
            private final LoadingCache<IPlayerProfile, SkinBundle> cache;
            private final List<ISkinProvider> providers;
            private final Consumer<IPlayerProfile> profileChangeListener;

            {
                reloading = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<SkinBundle, AtomicReference<Object>>() {

                    @Override
                    public AtomicReference<Object> load(SkinBundle key) throws Exception {
                        return new AtomicReference<>();
                    }

                });
                cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS).removalListener(new RemovalListener<IPlayerProfile, SkinBundle>() {

                    @Override
                    public void onRemoval(RemovalNotification<IPlayerProfile, SkinBundle> notification) {
                        SkinBundle skin = notification.getValue();
                        if (skin != null)
                            skin.onRemoval();
                    }

                }).build(new CacheLoader<IPlayerProfile, SkinBundle>() {

                    @Override
                    public SkinBundle load(IPlayerProfile key) throws Exception {
                        key.setUpdateListener(profileChangeListener);

                        return new SkinBundle().set(providers.stream().map(provider -> {
                            return provider.getSkin(key);
                        }).filter(skin -> {
                            return skin != null;
                        }).collect(Collectors.toCollection(ArrayList::new)));
                    }

                    @Override
                    public ListenableFuture<SkinBundle> reload(IPlayerProfile key, SkinBundle oldValue) throws Exception {
                        // Gather new ISkin objects.
                        Collection<ISkin> skins = providers.stream().map(provider -> {
                            return provider.getSkin(key);
                        }).filter(skin -> {
                            return skin != null;
                        }).collect(Collectors.toCollection(ArrayList::new));
                        // Prepare for monitoring.
                        Object token;
                        reloading.getUnchecked(oldValue).set(token = new Object());
                        long deadline = System.currentTimeMillis() + 10000; // 10 seconds
                        Supplier<Boolean> ready = () -> {
                            return System.currentTimeMillis() - deadline > 0L || reloading.getUnchecked(oldValue).get() != token || skins.stream().filter(ISkin::isDataReady).findAny().isPresent();
                        };
                        Runnable update = () -> {
                            if (reloading.getUnchecked(oldValue).compareAndSet(token, null))
                                oldValue.set(skins);
                        };

                        if (skins.isEmpty()) {
                            update.run();
                        } else {
                            ManagedBlocker blocker = new ManagedBlocker() {

                                @Override
                                public boolean block() throws InterruptedException {
                                    Thread.sleep(1000); // 1 second
                                    return ready.get();
                                }

                                @Override
                                public boolean isReleasable() {
                                    return ready.get();
                                }

                            };
                            SharedPool.execute(() -> {
                                try {
                                    ForkJoinPool.managedBlock(blocker);
                                } catch (InterruptedException e) {
                                } finally {
                                    update.run();
                                }
                            });
                        }
                        return Futures.immediateFuture(oldValue);
                    }

                });
                providers = new CopyOnWriteArrayList<>();
                profileChangeListener = profile -> {
                    if (cache.getIfPresent(profile) != null)
                        cache.refresh(profile);
                };
            }

            @Override
            public void clearProviders() {
                providers.clear();
                cache.invalidateAll();
            }

            @Override
            public ISkin getSkin(IPlayerProfile profile) {
                if (profile == null)
                    return DUMMY;
                return cache.getUnchecked(profile);
            }

            @Override
            public boolean registerProvider(ISkinProvider provider) {
                if (provider == null || provider == this)
                    return false;
                return providers.add(provider);
            }

        };
    }

}
