package lain.mods.skins.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            InputStream input = null;
            try
            {
                input = wrapByteBufferAsInputStream(data);
                BufferedImage image = ImageIO.read(input);
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
            finally
            {
                if (input != null)
                    input.close();
            }
        }
        catch (Throwable ignored)
        {
        }
        return "unknown";
    }

    public static ByteBuffer toBuffer(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        buf.put(data);
        buf.rewind();
        return buf;
    }

    public static InputStream wrapByteBufferAsInputStream(ByteBuffer original)
    {
        ByteBuffer buf = original.duplicate();
        return new InputStream()
        {

            @Override
            public int read() throws IOException
            {
                if (!buf.hasRemaining())
                    return -1;
                return buf.get() & 0xFF;
            }

            @Override
            public int read(byte[] bytes, int off, int len) throws IOException
            {
                if (!buf.hasRemaining())
                    return -1;
                len = Math.min(len, buf.remaining());
                buf.get(bytes, off, len);
                return len;
            }

        };
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

    public void put(byte[] data)
    {
        ByteBuffer buf = null;
        if (data != null)
        {
            buf = toBuffer(data);
            for (Function<ByteBuffer, ByteBuffer> filter : filters)
                if ((buf = filter.apply(buf)) == null)
                    break;
        }

        this.data = buf;
        this.type = judgeSkinType(buf);
    }

    public void put(byte[] data, String type)
    {
        ByteBuffer buf = null;
        if (data != null)
        {
            buf = toBuffer(data);
            for (Function<ByteBuffer, ByteBuffer> filter : filters)
                if ((buf = filter.apply(buf)) == null)
                    break;
        }

        this.data = buf;
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
