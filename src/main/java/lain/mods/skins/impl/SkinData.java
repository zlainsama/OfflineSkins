package lain.mods.skins.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.imageio.ImageIO;
import lain.mods.skins.api.interfaces.ISkin;

public class SkinData implements ISkin
{

    public static String judgeSkinType(ByteBuffer data)
    {
        try
        {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data.array()));
            int w = image.getWidth();
            int h = image.getHeight();
            if (w == h * 2)
                return "legacy";
            if (w == h)
            {
                int r = Math.max(w / 64, 1);
                if (((image.getRGB(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                    return "slim";
                return "default";
            }
        }
        catch (Throwable ignored)
        {
        }
        return "unknown";
    }

    private ByteBuffer data;
    private String type;
    private final Collection<Consumer<ISkin>> listeners = new ArrayList<>();
    private final Collection<Function<ByteBuffer, ByteBuffer>> filters = new ArrayList<>();

    @Override
    public ByteBuffer getData()
    {
        return data;
    }

    @Override
    public String getSkinType()
    {
        return type;
    }

    @Override
    public boolean isDataReady()
    {
        return data != null;
    }

    @Override
    public void onRemoval()
    {
        for (Consumer<ISkin> listener : listeners)
            listener.accept(this);

        data = null;
        type = null;
    }

    public void put(ByteBuffer data)
    {
        if (data != null)
        {
            for (Function<ByteBuffer, ByteBuffer> filter : filters)
                if ((data = filter.apply(data)) == null)
                    break;
        }

        this.data = data;
        this.type = judgeSkinType(data);
    }

    public void put(ByteBuffer data, String type)
    {
        if (data != null)
        {
            for (Function<ByteBuffer, ByteBuffer> filter : filters)
                if ((data = filter.apply(data)) == null)
                    break;
        }

        this.data = data;
        this.type = type;
    }

    @Override
    public boolean setRemovalListener(Consumer<ISkin> listener)
    {
        if (listener == null || listeners.contains(listener))
            return false;
        return listeners.add(listener);
    }

    @Override
    public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter)
    {
        if (filter == null || filters.contains(filter))
            return false;
        return filters.add(filter);
    }

}
