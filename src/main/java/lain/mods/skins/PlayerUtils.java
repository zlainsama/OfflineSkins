package lain.mods.skins;

import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import com.google.common.base.Charsets;
import com.google.common.collect.MapMaker;

public class PlayerUtils
{

    public static boolean isOfflinePlayer(EntityPlayer player)
    {
        if (!playerOnlineStatus.containsKey(player))
            playerOnlineStatus.put(player, !UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getGameProfile().getName()).getBytes(Charsets.UTF_8)).equals(player.getUniqueID()));
        return !playerOnlineStatus.get(player);
    }

    private static final Map<EntityPlayer, Boolean> playerOnlineStatus = new MapMaker().weakKeys().makeMap();

}
