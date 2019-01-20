package lain.mods.skins.impl;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.authlib.GameProfile;

public class Shared
{

    public static final GameProfile DUMMY = new GameProfile(UUID.fromString("ae9460f5-bf72-468e-89b6-4eead59001ad"), "");
    public static final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newWorkStealingPool());

    private static final Cache<UUID, Boolean> offlines = CacheBuilder.newBuilder().weakKeys().build();

    @SuppressWarnings("unchecked")
    public static <V> V blockyCall(Callable<V> task, V defaultValue, Consumer<Throwable> report)
    {
        Object[] result = new Object[2];
        try
        {
            ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker()
            {

                @Override
                public boolean block() throws InterruptedException
                {
                    try
                    {
                        result[0] = task.call();
                    }
                    catch (Throwable t)
                    {
                        if (result[1] == null)
                            result[1] = t;
                        else
                            ((Throwable) result[1]).addSuppressed(t);
                    }
                    return true;
                }

                @Override
                public boolean isReleasable()
                {
                    return false;
                }

            });
        }
        catch (Throwable t)
        {
            if (result[1] == null)
                result[1] = t;
            else
                ((Throwable) result[1]).addSuppressed(t);
        }
        if (result[1] != null)
        {
            if (report != null)
                report.accept((Throwable) result[1]);
            return defaultValue;
        }
        return (V) result[0];
    }

    public static byte[] blockyReadFile(File file, byte[] defaultContents, Consumer<Throwable> report)
    {
        return blockyCall(() -> {
            FileInputStream fis = null;
            ByteArrayOutputStream baos = null;
            try
            {
                (fis = new FileInputStream(file)).getChannel().transferTo(0, Long.MAX_VALUE, Channels.newChannel(baos = new ByteArrayOutputStream()));
                return baos.toByteArray();
            }
            finally
            {
                closeQuietly(baos);
                closeQuietly(fis);
            }
        }, defaultContents, report);
    }

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

    public static boolean isOfflinePlayer(UUID id, String name)
    {
        try
        {
            return offlines.get(id, () -> {
                if (isBlank(name))
                    return true;
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).equals(id);
            });
        }
        catch (ExecutionException e)
        {
            return true;
        }
    }

}
