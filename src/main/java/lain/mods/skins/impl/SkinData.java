package lain.mods.skins.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import lain.mods.skins.api.interfaces.ISkin;

public class SkinData implements ISkin
{

    public static ByteBuffer toBuffer(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        buf.put(data);
        buf.rewind();
        return buf;
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
