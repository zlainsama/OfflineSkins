package lain.mods.skins.api.interfaces;

public interface ISkinProvider
{

    /**
     * @param profile the profile that is queried about.
     * @return the ISkin object for the profile, null if unavailable.
     */
    ISkin getSkin(IPlayerProfile profile);

}
