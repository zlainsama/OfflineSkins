package lain.mods.skins.api.interfaces;

public interface ISkinProvider {

    /**
     * This must not be a heavy blocking task as this will be run from main thread.
     *
     * @param profile the profile that is queried about.
     * @return the ISkin object for the profile, null if unavailable.
     */
    ISkin getSkin(IPlayerProfile profile);

}
