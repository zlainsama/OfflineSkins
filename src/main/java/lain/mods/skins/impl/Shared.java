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

    /**
     * Call a possibly blocking task in a ManagedBlocker to allow current Thread adjust if it is a ForkJoinWorkerThread.
     *
     * @param task         the task to call.
     * @param defaultValue a default value to return if the task failed during the call.
     * @param receiver     a consumer that will receive a Throwable if the task failed or interrupted during the call, null is acceptable.
     * @return the result of the task or defaultValue if it failed during the call.
     */
    @SuppressWarnings("unchecked")
    public static <V> V blockyCall(Callable<V> task, V defaultValue, Consumer<Throwable> receiver)
    {
        if (task == null)
            return defaultValue;
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
            if (receiver != null)
                receiver.accept((Throwable) result[1]);
            return defaultValue;
        }
        return (V) result[0];
    }

    /**
     * Completely read a file in a ManagedBlocker to allow current Thread adjust if it is a ForkJoinWorkerThread.
     *
     * @param file            the file to read.
     * @param defaultContents a default value to return if failed during reading the file.
     * @param receiver        a consumer that will receive a Throwable if failed or interrupted during reading the file, null is acceptable.
     * @return the contents of the file or defaultContents if failed during reading the file.
     */
    public static byte[] blockyReadFile(File file, byte[] defaultContents, Consumer<Throwable> receiver)
    {
        if (file == null)
            return defaultContents;
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
        }, defaultContents, receiver);
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
        if (id == null || isBlank(name)) // treat incomplete profiles as offline profiles, but don't cache results for them as they can be updated later and possibly become online profiles.
            return true;
        try
        {
            return offlines.get(id, () -> {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).equals(id);
            });
        }
        catch (Throwable t)
        {
            return true;
        }
    }

}
