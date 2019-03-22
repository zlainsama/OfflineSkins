package lain.mods.skins.init.forge.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.LineProcessor;
import LZMA.LzmaInputStream;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

public class Setup implements IFMLCallHook
{

    public static BiMap<String, String> PackageMap;
    public static BiMap<String, String> ClassMap;
    public static BiMap<List<String>, List<String>> FieldMap;
    public static BiMap<List<String>, List<String>> MethodMap;

    private String deobfuscationFileName;

    @Override
    public Void call() throws Exception
    {
        try
        {
            PackageMap = HashBiMap.create();
            ClassMap = HashBiMap.create();
            FieldMap = HashBiMap.create();
            MethodMap = HashBiMap.create();
            new ByteSource()
            {

                @Override
                public InputStream openStream() throws IOException
                {
                    return new LzmaInputStream(Setup.class.getResourceAsStream(deobfuscationFileName));
                }

            }.asCharSource(Charsets.UTF_8).readLines(new LineProcessor<Void>()
            {

                @Override
                public Void getResult()
                {
                    return null;
                }

                @Override
                public boolean processLine(String line) throws IOException
                {
                    if (line.startsWith("PK: "))
                    {
                        line = line.substring(4);
                        String[] data = line.split(" ");
                        if (data.length == 2)
                            PackageMap.put(data[0], data[1]);
                    }
                    else if (line.startsWith("CL: "))
                    {
                        line = line.substring(4);
                        String[] data = line.split(" ");
                        if (data.length == 2)
                            ClassMap.put(data[0], data[1]);
                    }
                    else if (line.startsWith("FD: "))
                    {
                        line = line.substring(4);
                        String[] data = line.split(" ");
                        if (data.length == 2)
                        {
                            int i0 = data[0].lastIndexOf("/");
                            int i1 = data[1].lastIndexOf("/");
                            if (i0 != -1 && i1 != -1)
                                FieldMap.put(ImmutableList.of(data[0].substring(0, i0), data[0].substring(i0 + 1, data[0].length())), ImmutableList.of(data[1].substring(0, i1), data[1].substring(i1 + 1, data[1].length())));
                        }
                    }
                    else if (line.startsWith("MD: "))
                    {
                        line = line.substring(4);
                        String[] data = line.split(" ");
                        if (data.length == 4)
                        {
                            int i0 = data[0].lastIndexOf("/");
                            int i2 = data[2].lastIndexOf("/");
                            if (i0 != -1 && i2 != -1)
                                MethodMap.put(ImmutableList.of(data[0].substring(0, i0), data[0].substring(i0 + 1, data[0].length()), data[1]), ImmutableList.of(data[2].substring(0, i2), data[2].substring(i2 + 1, data[2].length()), data[3]));
                        }
                    }
                    return true;
                }

            });
            PackageMap = ImmutableBiMap.copyOf(PackageMap);
            ClassMap = ImmutableBiMap.copyOf(ClassMap);
            FieldMap = ImmutableBiMap.copyOf(FieldMap);
            MethodMap = ImmutableBiMap.copyOf(MethodMap);
        }
        catch (Exception e)
        {
            PackageMap = ImmutableBiMap.of();
            ClassMap = ImmutableBiMap.of();
            FieldMap = ImmutableBiMap.of();
            MethodMap = ImmutableBiMap.of();
            throw e;
        }
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        deobfuscationFileName = (String) data.get("deobfuscationFileName");
    }

}
