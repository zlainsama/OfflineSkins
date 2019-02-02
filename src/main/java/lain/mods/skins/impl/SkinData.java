package lain.mods.skins.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.imageio.ImageIO;
import lain.mods.skins.api.interfaces.ISkin;

public class SkinData implements ISkin
{

    public static String judgeSkinType(byte[] data)
    {
        try (InputStream input = new ByteArrayInputStream(data))
        {
            BufferedImage image = ImageIO.read(input);
            int w = image.getWidth();
            int h = image.getHeight();
            if (w == h * 2)
                return "default"; // it's actually "legacy", but there will always be a filter to convert them into "default".
            if (w == h)
            {
                int r = Math.max(w / 64, 1);
                if (((image.getRGB(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                    return "slim";
                return "default";
            }
            return "unknown";
        }
        catch (Throwable t)
        {
            return "unknown";
        }
    }

    public static String judgeSkinType(ByteBuffer data)
    {
        try (InputStream input = wrapByteBufferAsInputStream(data))
        {
            BufferedImage image = ImageIO.read(input);
            int w = image.getWidth();
            int h = image.getHeight();
            if (w == h * 2)
                return "default"; // it's actually "legacy", but there will always be a filter to convert them into "default".
            if (w == h)
            {
                int r = Math.max(w / 64, 1);
                if (((image.getRGB(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                    return "slim";
                return "default";
            }
            return "unknown";
        }
        catch (Throwable t)
        {
            return "unknown";
        }
    }

    public static ByteBuffer toBuffer(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        buf.put(data);
        buf.rewind();
        return buf;
    }

    public static boolean validateData(byte[] data)
    {
        try (InputStream input = new ByteArrayInputStream(data))
        {
            return ImageIO.read(input) != null;
        }
        catch (Throwable t)
        {
            return false;
        }
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
    private final Collection<Consumer<ISkin>> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Function<ByteBuffer, ByteBuffer>> filters = new CopyOnWriteArrayList<>();

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
    public synchronized void onRemoval()
    {
        for (Consumer<ISkin> listener : listeners)
            listener.accept(this);

        data = null;
        type = null;
    }

    public synchronized void put(byte[] data, String type)
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
