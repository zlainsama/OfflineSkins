package lain.mods.skins.api;

import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.entity.AbstractClientPlayer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;

public class SkinProviderAPI
{

    public static ISkinProviderService createService()
    {
        return new ISkinProviderService()
        {

            private final List<ISkinProvider> providers = Lists.newArrayList();
            private final LoadingCache<AbstractClientPlayer, List<ISkin>> cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS).removalListener(new RemovalListener<AbstractClientPlayer, List<ISkin>>()
            {

                @Override
                public void onRemoval(RemovalNotification<AbstractClientPlayer, List<ISkin>> notification)
                {
                    List<ISkin> list = notification.getValue();
                    if (list != null)
                    {
                        for (ISkin skin : list)
                            skin.onRemoval();
                    }
                }

            }).build(new CacheLoader<AbstractClientPlayer, List<ISkin>>()
            {

                @Override
                public List<ISkin> load(AbstractClientPlayer key) throws Exception
                {
                    List<ISkin> list = Lists.newArrayList();
                    for (ISkinProvider p : providers)
                    {
                        ISkin s = p.getSkin(key);
                        if (s != null)
                            list.add(s);
                    }
                    return list;
                }

            });

            @Override
            public void clear()
            {
                providers.clear();
                cache.invalidateAll();
            }

            @Override
            public ISkin getSkin(AbstractClientPlayer player)
            {
                List<ISkin> list = cache.getUnchecked(player);
                for (ISkin skin : list)
                {
                    if (skin.isSkinReady())
                        return skin;
                }
                return null;
            }

            @Override
            public void register(ISkinProvider provider)
            {
                if (provider == null || provider == this)
                    throw new UnsupportedOperationException();
                providers.add(provider);
            }

        };
    }

}
