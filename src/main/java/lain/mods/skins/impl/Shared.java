package lain.mods.skins.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import lain.lib.SharedPool;

public class Shared
{

    private static interface SupplierBlocker<T> extends Supplier<T>, ForkJoinPool.ManagedBlocker
    {
    }

    public static final GameProfile DUMMY = new GameProfile(UUID.fromString("ae9460f5-bf72-468e-89b6-4eead59001ad"), "");
    public static final Map<String, String> store = new ConcurrentHashMap<>();

    private static final Cache<UUID, Boolean> offlines = CacheBuilder.newBuilder().weakKeys().build();

    /**
     * Call a possibly blocking task in a ManagedBlocker to allow current Thread adjust if it is a ForkJoinWorkerThread.
     *
     * @param callable     the task to call.
     * @param defaultValue a default value to return if the task failed during the call.
     * @param consumer     a consumer that will receive a Throwable if the task failed during the call, null is acceptable.
     * @return the result of the task or defaultValue if it failed during the call.
     */
    public static <T> T blockyCall(Callable<T> callable, T defaultValue, Consumer<Throwable> consumer)
    {
        if (callable == null)
            return defaultValue;
        return new SupplierBlocker<T>()
        {

            T result;

            @Override
            public boolean block() throws InterruptedException
            {
                try
                {
                    result = callable.call();
                }
                catch (Throwable t)
                {
                    if (consumer != null)
                        consumer.accept(t);
                    result = defaultValue;
                }
                return true;
            }

            @Override
            public T get()
            {
                try
                {
                    ForkJoinPool.managedBlock(this);
                }
                catch (InterruptedException e)
                {
                    if (consumer != null)
                        consumer.accept(e);
                }
                return result;
            }

            @Override
            public boolean isReleasable()
            {
                return false;
            }

        }.get();
    }

    /**
     * Completely read a file in a ManagedBlocker to allow current Thread adjust if it is a ForkJoinWorkerThread.
     *
     * @param file            the file to read.
     * @param defaultContents a default value to return if failed during reading the file.
     * @param consumer        a consumer that will receive a Throwable if failed during reading the file, null is acceptable.
     * @return the contents of the file or defaultContents if failed during reading the file.
     */
    public static byte[] blockyReadFile(File file, byte[] defaultContents, Consumer<Throwable> consumer)
    {
        if (file == null)
            return defaultContents;
        return blockyCall(() -> {
            try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                fis.getChannel().transferTo(0, Long.MAX_VALUE, Channels.newChannel(baos));
                return baos.toByteArray();
            }
        }, defaultContents, consumer);
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

    public static boolean sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
            return true;
        }
        catch (InterruptedException e)
        {
            return false;
        }
    }

    public static <T> ListenableFuture<T> submitTask(Callable<T> callable)
    {
        ListenableFutureTask<T> future;
        SharedPool.execute(future = ListenableFutureTask.create(callable));
        return future;
    }

}
