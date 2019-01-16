package lain.mods.skins.providers;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lain.mods.skins.api.interfaces.IPlayerProfile;

public class Shared
{

    private static final Executor pool = Executors.newCachedThreadPool();
    private static final Map<UUID, Boolean> offline = new WeakHashMap<>();

    protected static void execute(Runnable task)
    {
        pool.execute(task);
    }

    protected static boolean isBlank(CharSequence cs)
    {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0)
            return true;
        for (int i = 0; i < strLen; i++)
            if (!Character.isWhitespace(cs.charAt(i)))
                return false;
        return true;
    }

    protected static boolean isOfflinePlayerProfile(IPlayerProfile profile)
    {
        UUID id = profile.getPlayerID();
        if (!offline.containsKey(id))
            offline.put(id, UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getPlayerName()).getBytes(StandardCharsets.UTF_8)).equals(id));
        return offline.get(id);
    }

}
