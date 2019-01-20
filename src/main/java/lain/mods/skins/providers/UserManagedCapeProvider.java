package lain.mods.skins.providers;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;

public class UserManagedCapeProvider implements ISkinProvider
{

    private File _dirN;
    private File _dirU;
    private Function<ByteBuffer, ByteBuffer> _filter;

    public UserManagedCapeProvider(Path workDir)
    {
        _dirN = new File(workDir.toFile(), "capes");
        _dirN.mkdirs();
        _dirU = new File(_dirN, "uuid");
        _dirU.mkdirs();
    }

    @Override
    public ISkin getSkin(IPlayerProfile profile)
    {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        Shared.pool.execute(() -> {
            byte[] data = null;
            if (!Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName()))
                data = Shared.blockyCall(() -> {
                    return readFile(_dirU, "%s.png", profile.getPlayerID().toString().replaceAll("-", ""));
                }, null, null);
            if (data == null && !Shared.isBlank(profile.getPlayerName()))
                data = Shared.blockyCall(() -> {
                    return readFile(_dirN, "%s.png", profile.getPlayerName());
                }, null, null);
            if (data != null)
                skin.put(data, "cape");
        });
        return skin;
    }

    private byte[] readFile(File dir, String filename)
    {
        return Shared.blockyReadFile(new File(dir, filename), null, null);
    }

    private byte[] readFile(File dir, String filename, Object... args)
    {
        return readFile(dir, String.format(filename, args));
    }

    public UserManagedCapeProvider withFilter(Function<ByteBuffer, ByteBuffer> filter)
    {
        _filter = filter;
        return this;
    }

}
