package lain.mods.skins.api;

import lain.mods.skins.api.interfaces.ISkin;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A special ISkin object that will return first ready ISkin object in a collection. <br>
 * It supports swapping it's collection reference at runtime.
 */
public class SkinBundle implements ISkin {

    protected final AtomicReference<Collection<ISkin>> ref = new AtomicReference<>(Collections.emptyList());
    protected final Collection<Consumer<ISkin>> listeners = new CopyOnWriteArrayList<>();
    protected final Collection<Function<ByteBuffer, ByteBuffer>> filters = new CopyOnWriteArrayList<>();

    protected Optional<ISkin> find() {
        Collection<ISkin> skins;
        if ((skins = ref.get()).isEmpty())
            return Optional.empty();
        return skins.stream().filter(ISkin::isDataReady).findFirst();
    }

    @Override
    public ByteBuffer getData() {
        return find().orElse(SkinProviderAPI.DUMMY).getData();
    }

    @Override
    public String getSkinType() {
        return find().orElse(SkinProviderAPI.DUMMY).getSkinType();
    }

    @Override
    public boolean isDataReady() {
        return find().orElse(SkinProviderAPI.DUMMY).isDataReady();
    }

    @Override
    public void onRemoval() {
        set(Collections.emptyList());
    }

    public SkinBundle set(Collection<ISkin> c) {
        Objects.requireNonNull(c);
        if (!c.isEmpty()) {
            for (ISkin e : c) {
                listeners.forEach(e::setRemovalListener);
                filters.forEach(e::setSkinFilter);
            }
        }
        Collection<ISkin> skins;
        if (!(skins = ref.getAndSet(c)).isEmpty())
            skins.forEach(ISkin::onRemoval);
        return this;
    }

    @Override
    public boolean setRemovalListener(Consumer<ISkin> listener) {
        if (listener == null || listeners.contains(listener))
            return false;
        if (listeners.add(listener)) {
            Collection<ISkin> skins;
            if (!(skins = ref.get()).isEmpty())
                skins.forEach(e -> e.setRemovalListener(listener));
            return true;
        }
        return false;
    }

    @Override
    public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter) {
        if (filter == null || filters.contains(filter))
            return false;
        if (filters.add(filter)) {
            Collection<ISkin> skins;
            if (!(skins = ref.get()).isEmpty())
                skins.forEach(e -> e.setSkinFilter(filter));
            return true;
        }
        return false;
    }

}
