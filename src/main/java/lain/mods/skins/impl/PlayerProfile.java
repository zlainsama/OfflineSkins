package lain.mods.skins.impl;

import java.lang.ref.WeakReference;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class PlayerProfile implements IPlayerProfile
{

    private WeakReference<GameProfile> p;

    public PlayerProfile(GameProfile profile)
    {
        p = new WeakReference<GameProfile>(profile);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof PlayerProfile)
            return p.get().equals(((PlayerProfile) o).p.get());
        return false;
    }

    @Override
    public UUID getPlayerID()
    {
        return p.get().getId();
    }

    @Override
    public String getPlayerName()
    {
        return p.get().getName();
    }

    @Override
    public int hashCode()
    {
        return p.get().hashCode();
    }

}
