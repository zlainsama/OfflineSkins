package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public class ImageCache
{

    private final List<ImageSupplier> suppliers = Lists.newArrayList();
    private final ExecutorService pool = new ThreadPoolExecutor(0, 2, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    private final LoadingCache<String, Optional<BufferedImage>> cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS).build(new CacheLoader<String, Optional<BufferedImage>>()
    {

        @Override
        public Optional<BufferedImage> load(String key) throws Exception
        {
            final String target = key;
            pool.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    BufferedImage image = null;
                    for (ImageSupplier supplier : suppliers)
                        if ((image = supplier.loadImage(target)) != null)
                            break;
                    if (image != null)
                        cache.put(target, Optional.of(image));
                }

            });
            return Optional.absent();
        }

    });

    public void addSupplier(ImageSupplier supplier)
    {
        suppliers.add(supplier);
    }

    public BufferedImage get(String name)
    {
        return cache.getUnchecked(name).orNull();
    }

}
