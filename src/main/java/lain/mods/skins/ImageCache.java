package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public class ImageCache
{

    private final List<ImageSupplier> suppliers = Lists.newArrayList();
    private final LoadingCache<String, BufferedImage> cache = CacheBuilder.newBuilder().maximumSize(512).expireAfterWrite(20, TimeUnit.MINUTES).build(new CacheLoader<String, BufferedImage>()
    {

        @Override
        public BufferedImage load(String key) throws Exception
        {
            BufferedImage image = null;
            for (ImageSupplier supplier : suppliers)
                if ((image = supplier.loadImage(key)) != null)
                    break;
            return image;
        }

    });

    public void addSupplier(ImageSupplier supplier)
    {
        suppliers.add(supplier);
    }

    public BufferedImage get(String name)
    {
        return cache.getUnchecked(name);
    }

}
