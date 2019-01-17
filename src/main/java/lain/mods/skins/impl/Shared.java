package lain.mods.skins.impl;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class Shared
{

    public static final GameProfile DUMMY = new GameProfile(UUID.fromString("ae9460f5-bf72-468e-89b6-4eead59001ad"), "");
    public static final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    private static final Map<UUID, Boolean> offline = new WeakHashMap<>();

    public static void closeQuietly(Closeable c)
    {
        try
        {
            if (c != null)
                c.close();
        }
        catch (IOException ignored)
        {
        }
    }

    public static boolean isBlank(CharSequence cs)
    {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0)
            return true;
        for (int i = 0; i < strLen; i++)
            if (!Character.isWhitespace(cs.charAt(i)))
                return false;
        return true;
    }

    public static boolean isOfflinePlayerProfile(IPlayerProfile profile)
    {
        UUID id = profile.getPlayerID();
        if (!offline.containsKey(id))
            offline.put(id, UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getPlayerName()).getBytes(StandardCharsets.UTF_8)).equals(id));
        return offline.get(id);
    }

}
