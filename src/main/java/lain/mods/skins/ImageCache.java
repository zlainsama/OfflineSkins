package lain.mods.skins;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ImageCache
{

    private final Map<String, BufferedImage> map = Maps.newHashMap();
    private final List<ImageSupplier> suppliers = Lists.newArrayList();

    public void addSupplier(ImageSupplier supplier)
    {
        suppliers.add(supplier);
    }

    public void clear()
    {
        map.clear();
    }

    public void clear(String name)
    {
        map.remove(name);
    }

    public BufferedImage get(String name)
    {
        return get(name, false);
    }

    public BufferedImage get(String name, boolean force)
    {
        if (!map.containsKey(name) || force)
        {
            BufferedImage image = null;
            for (ImageSupplier supplier : suppliers)
                if ((image = supplier.loadImage(name)) != null)
                    break;
            map.put(name, image);
        }
        return map.get(name);
    }

}
