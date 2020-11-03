package lain.mods.skins.api.interfaces;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ISkin {

    /**
     * @return the ByteBuffer for the skin.
     */
    ByteBuffer getData();

    /**
     * @return "default" for classical 4-pixel arms, "slim" for 3-pixel slim arms, "legacy" for old skin format, "cape" for capes. (note that "legacy" and "cape" are not official things)
     */
    String getSkinType();

    /**
     * @return true if the ByteBuffer is ready for use.
     */
    boolean isDataReady();

    /**
     * Do cleanup when this gets called. <br>
     * Listeners will be notified before anything is done, and then, resources will be released.
     */
    void onRemoval();

    /**
     * Set a listener to be notified when {@link #onRemoval() onRemoval()} is called, and before anything is done to the resources. <br>
     * Multiple listeners will be called one by one in order.
     *
     * @param listener the listener to set.
     * @return true if successful, null and duplicates will fail.
     */
    boolean setRemovalListener(Consumer<ISkin> listener);

    /**
     * Set a filter to perform an action on the data and possibly transform it before it got pushed to the game. <br>
     * The returned buffer will be used instead of the original. <br>
     * Multiple filters will be applied one by one in a chain. <br>
     * Don't forget to {@link ByteBuffer#rewind() rewind()} before return it if you modified it's state. <br>
     * Make sure the final buffer is a direct buffer, see {@link org.lwjgl.BufferUtils BufferUtils}, otherwise the game will fail.
     *
     * @param filter the filter to set.
     * @return true if successful, null and duplicates will fail.
     */
    boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter);

}
