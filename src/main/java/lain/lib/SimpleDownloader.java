package lain.lib;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SimpleDownloader {

    private SimpleDownloader() {
        throw new Error("NoInstance");
    }

    private static Optional<URLConnection> connect(URL url, Proxy proxy, Consumer<URLConnection> preConnect, Consumer<Throwable> onException) {
        try {
            URLConnection conn = proxy == null ? url.openConnection() : url.openConnection(proxy);
            if (preConnect != null)
                preConnect.accept(conn);
            conn.connect();
            return Optional.of(conn);
        } catch (Throwable e) {
            if (onException != null)
                onException.accept(e);
            return Optional.empty();
        }
    }

    private static boolean deleteIfExists(Path path, Consumer<Throwable> onException) {
        try {
            return Files.deleteIfExists(path);
        } catch (Throwable e) {
            if (onException != null)
                onException.accept(e);
            return false;
        }
    }

    private static <T extends Throwable> Consumer<T> deleteOnExceptionDecor(Path path, Consumer<T> onException) {
        Consumer<T> deleteOnException = e -> deleteIfExists(path, SimpleDownloader::rethrowIfNonIOException);
        return onException == null ? deleteOnException : deleteOnException.andThen(onException);
    }

    private static <T> Predicate<T> deleteOnFalseDecor(Path path, Predicate<T> predicate) {
        if (predicate == null)
            return null;
        return t -> {
            if (predicate.test(t))
                return true;
            deleteIfExists(path, SimpleDownloader::rethrowIfNonIOException);
            return false;
        };
    }

    private static Optional<Path> download(Path local, URLConnection conn, MessageDigest digest, Predicate<URLConnection> shouldTransfer, Consumer<Throwable> onException) {
        try {
            if (shouldTransfer != null && !shouldTransfer.test(conn))
                return Optional.empty();
            try (FileChannel channel = FileChannel.open(local, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                channel.transferFrom(Channels.newChannel(digest == null ? conn.getInputStream() : new DigestInputStream(conn.getInputStream(), digest)), 0L, Long.MAX_VALUE);
                return Optional.of(local);
            }
        } catch (Throwable e) {
            if (digest != null)
                digest.reset();
            if (onException != null)
                onException.accept(e);
            return Optional.empty();
        }
    }

    private static Optional<URL> resource(String resource, Consumer<Throwable> onException) {
        try {
            return Optional.of(new URL(resource));
        } catch (Throwable e) {
            if (onException != null)
                onException.accept(e);
            return Optional.empty();
        }
    }

    private static void rethrowIfNonIOException(Throwable throwable) {
        if (throwable instanceof IOException)
            return;
        Retries.rethrow(throwable);
    }

    private static void runAsync(Runnable runnable, Executor executor) {
        if (executor == null)
            CompletableFuture.runAsync(runnable);
        else
            CompletableFuture.runAsync(runnable, executor);
    }

    private static boolean sleep(long millis, Consumer<Throwable> onException) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (Throwable e) {
            if (onException != null)
                onException.accept(e);
            return false;
        }
    }

    public static CompletableFuture<Optional<Path>> start(String resource) {
        return start(resource, null, null, 2, null, SharedPool::execute, null, null, null);
    }

    public static CompletableFuture<Optional<Path>> start(String resource, Path tempDir, Proxy proxy, int maxRetries, MessageDigest digest, Executor executor, Consumer<Thread> preExecute, Consumer<URLConnection> preConnect, Predicate<URLConnection> shouldTransfer) {
        Objects.requireNonNull(resource);
        CompletableFuture<Optional<Path>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                if (!future.isDone()) {
                    if (preExecute != null)
                        preExecute.accept(Thread.currentThread());
                    resource(resource, future::completeExceptionally).ifPresent(remote -> {
                        Retries.retrying(() -> {
                            if (!future.isDone()) {
                                connect(remote, proxy, preConnect, Retries::rethrow).ifPresent(conn -> {
                                    tempFile(tempDir, future::completeExceptionally).ifPresent(local -> {
                                        download(local, conn, digest, deleteOnFalseDecor(local, shouldTransfer), deleteOnExceptionDecor(local, Retries::rethrow)).ifPresent(result -> future.complete(Optional.of(result)));
                                    });
                                });
                            }
                        }, IOException.class::isInstance, retries -> sleep(1000L, Retries::rethrow), maxRetries).toRunnable(future::completeExceptionally).run();
                    });
                }
            } finally {
                if (!future.isDone())
                    future.complete(Optional.empty());
            }
        }, executor);
        return future;
    }

    private static Optional<Path> tempFile(Path tempDir, Consumer<Throwable> onException) {
        try {
            return Optional.of(tempDir == null ? Files.createTempFile(null, null) : Files.createTempFile(tempDir, null, null));
        } catch (Throwable e) {
            if (onException != null)
                onException.accept(e);
            return Optional.empty();
        }
    }

}
