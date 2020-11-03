package lain.mods.skins.api.interfaces;

import java.util.UUID;
import java.util.function.Consumer;

public interface IPlayerProfile {

    /**
     * @return the actual profile object.
     */
    Object getOriginal();

    /**
     * @return the uuid of the profile.
     */
    UUID getPlayerID();

    /**
     * @return the name of the profile.
     */
    String getPlayerName();

    /**
     * Set a listener to be notified after this IPlayerProfile changes. (examples: from offline to online, from incomplete to complete) <br>
     * Multiple listeners will be called one by one in order. <br>
     * Be careful to not accumulate a large number of listeners as a profile will most likely exist a long time.
     *
     * @param listener the listener to set.
     * @return true if successful, null and duplicates will fail.
     */
    boolean setUpdateListener(Consumer<IPlayerProfile> listener);

}
