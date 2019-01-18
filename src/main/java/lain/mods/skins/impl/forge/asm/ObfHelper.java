package lain.mods.skins.impl.forge.asm;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import com.google.common.collect.ImmutableList;

public class ObfHelper
{

    public static ObfHelper newClass(String className)
    {
        Validate.notNull(className);
        ObfHelper result = new ObfHelper();
        result.type = 2;
        result.data[0] = className;
        result.transform(!Plugin.runtimeDeobfuscationEnabled);
        return result;
    }

    public static ObfHelper newField(String fieldName, String ownerName, String descriptor)
    {
        Validate.notNull(fieldName);
        Validate.notNull(ownerName);
        Validate.notNull(descriptor);
        ObfHelper result = new ObfHelper();
        result.type = 3;
        result.data[0] = ownerName;
        result.data[1] = fieldName;
        result.data[2] = descriptor;
        result.transform(!Plugin.runtimeDeobfuscationEnabled);
        return result;
    }

    public static ObfHelper newMethod(String methodName, String ownerName, String descriptor)
    {
        Validate.notNull(methodName);
        Validate.notNull(ownerName);
        Validate.notNull(descriptor);
        ObfHelper result = new ObfHelper();
        result.type = 4;
        result.data[0] = ownerName;
        result.data[1] = methodName;
        result.data[2] = descriptor;
        result.transform(!Plugin.runtimeDeobfuscationEnabled);
        return result;
    }

    public static ObfHelper newPackage(String packageName)
    {
        Validate.notNull(packageName);
        ObfHelper result = new ObfHelper();
        result.type = 1;
        result.data[0] = packageName;
        result.transform(!Plugin.runtimeDeobfuscationEnabled);
        return result;
    }

    private int type;
    private String[] data = new String[3];

    private ObfHelper()
    {
    }

    public String getData(int index)
    {
        if (index >= 0 && index <= 2)
            return data[index];
        return null;
    }

    public int getType()
    {
        return type;
    }

    public boolean match(Object... obj)
    {
        switch (type)
        {
            case 1:
                if (obj.length == 1)
                    return data[0].equals(obj[0]);
                break;
            case 2:
                if (obj.length == 1)
                    return data[0].equals(obj[0]);
                break;
            case 3:
                if (obj.length == 1)
                    return data[1].equals(obj[0]);
                else if (obj.length == 2)
                    return data[0].equals(obj[0]) && data[1].equals(obj[1]);
                break;
            case 4:
                if (obj.length == 2)
                    return data[1].equals(obj[0]) && data[2].equals(obj[1]);
                else if (obj.length == 3)
                    return data[0].equals(obj[0]) && data[1].equals(obj[1]) && data[2].equals(obj[2]);
                break;
        }
        return false;
    }

    public ObfHelper setDevName(String name)
    {
        return setDevName(name, Plugin.isDevelopmentEnvironment);
    }

    private ObfHelper setDevName(String name, boolean devEnv)
    {
        if (devEnv)
        {
            switch (type)
            {
                case 1:
                    data[0] = name;
                    break;
                case 2:
                    data[0] = name;
                    break;
                case 3:
                    data[1] = name;
                    break;
                case 4:
                    data[1] = name;
                    break;
            }
        }
        return this;
    }

    @Override
    public String toString()
    {
        switch (type)
        {
            case 1:
                return String.format("ObfHelper:Package{%s}", data[0]);
            case 2:
                return String.format("ObfHelper:Class{%s}", data[0]);
            case 3:
                return String.format("ObfHelper:Field{%s/%s %s}", data[0], data[1], data[2]);
            case 4:
                return String.format("ObfHelper:Method{%s/%s %s}", data[0], data[1], data[2]);
        }
        return String.format("ObfHelper:Unknown(%d){%s %s %s}", type, data[0], data[1], data[2]);
    }

    private void transform(boolean deobfuscated)
    {
        switch (type)
        {
            case 1:
                Map<String, String> map1 = deobfuscated ? Setup.PackageMap : Setup.PackageMap.inverse();
                if (map1.containsKey(data[0]))
                    data[0] = map1.get(data[0]);
                break;
            case 2:
                Map<String, String> map2 = deobfuscated ? Setup.ClassMap : Setup.ClassMap.inverse();
                if (map2.containsKey(data[0]))
                    data[0] = map2.get(data[0]);
                break;
            case 3:
                Map<List<String>, List<String>> map3 = deobfuscated ? Setup.FieldMap : Setup.FieldMap.inverse();
                List<String> list3 = ImmutableList.of(data[0], data[1]);
                if (map3.containsKey(list3))
                {
                    list3 = map3.get(list3);
                    data[0] = list3.get(0);
                    data[1] = list3.get(1);
                }
                break;
            case 4:
                Map<List<String>, List<String>> map4 = deobfuscated ? Setup.MethodMap : Setup.MethodMap.inverse();
                List<String> list4 = ImmutableList.of(data[0], data[1], data[2]);
                if (map4.containsKey(list4))
                {
                    list4 = map4.get(list4);
                    data[0] = list4.get(0);
                    data[1] = list4.get(1);
                    data[2] = list4.get(2);
                }
                break;
        }
    }

}
