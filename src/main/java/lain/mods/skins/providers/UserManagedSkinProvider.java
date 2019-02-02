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

public class UserManagedSkinProvider implements ISkinProvider
{

    private File _dirN;
    private File _dirU;
    private Function<ByteBuffer, ByteBuffer> _filter;

    public UserManagedSkinProvider(Path workDir)
    {
        _dirN = new File(workDir.toFile(), "skins");
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
                data = readFile(_dirU, "%s.png", profile.getPlayerID().toString().replaceAll("-", ""));
            if (data == null && !Shared.isBlank(profile.getPlayerName()))
                data = readFile(_dirN, "%s.png", profile.getPlayerName());
            if (data != null)
                skin.put(data, SkinData.judgeSkinType(data));
        });
        return skin;
    }

    private byte[] readFile(File dir, String filename)
    {
        byte[] contents;
        if ((contents = Shared.blockyReadFile(new File(dir, filename), null, null)) != null && SkinData.validateData(contents))
            return contents;
        return null;
    }

    private byte[] readFile(File dir, String filename, Object... args)
    {
        return readFile(dir, String.format(filename, args));
    }

    public UserManagedSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter)
    {
        _filter = filter;
        return this;
    }

}
