package lain.mods.skins.impl;

import java.lang.ref.WeakReference;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class PlayerProfile implements IPlayerProfile
{

    private WeakReference<GameProfile> _profile;

    public PlayerProfile(GameProfile profile)
    {
        if (profile == null)
            throw new IllegalArgumentException("profile must not be null");
        _profile = new WeakReference<GameProfile>(profile);
    }

    @Override
    public boolean equals(Object o)
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return false;
        if (o instanceof PlayerProfile)
            return p.equals(((PlayerProfile) o)._profile.get());
        return false;
    }

    @Override
    public UUID getPlayerID()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return Shared.DUMMY.getId();
        return p.getId();
    }

    @Override
    public String getPlayerName()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return Shared.DUMMY.getName();
        return p.getName();
    }

    @Override
    public int hashCode()
    {
        GameProfile p;
        if ((p = _profile.get()) == null) // gc
            return 0;
        return p.hashCode();
    }

}
