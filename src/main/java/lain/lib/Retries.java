package lain.lib;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Retries {

    private Retries() {
        throw new Error("NoInstance");
    }

    public static <T> ThrowingAccept<T> fallback(ThrowingAccept<T> action, ThrowingAccept<T> other) {
        return t -> {
            try {
                action.accept(t);
            } catch (Throwable throwable) {
                try {
                    other.accept(t);
                } catch (Throwable otherThrowable) {
                    throwable.addSuppressed(otherThrowable);
                    throw throwable;
                }
            }
        };
    }

    public static <T, R> ThrowingApply<T, R> fallback(ThrowingApply<T, R> action, ThrowingApply<T, R> other) {
        return t -> {
            try {
                return action.apply(t);
            } catch (Throwable throwable) {
                try {
                    return other.apply(t);
                } catch (Throwable otherThrowable) {
                    throwable.addSuppressed(otherThrowable);
                    throw throwable;
                }
            }
        };
    }

    public static <T> ThrowingGet<T> fallback(ThrowingGet<T> action, ThrowingGet<T> other) {
        return () -> {
            try {
                return action.get();
            } catch (Throwable throwable) {
                try {
                    return other.get();
                } catch (Throwable otherThrowable) {
                    throwable.addSuppressed(otherThrowable);
                    throw throwable;
                }
            }
        };
    }

    public static ThrowingRun fallback(ThrowingRun action, ThrowingRun other) {
        return () -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                try {
                    other.run();
                } catch (Throwable otherThrowable) {
                    throwable.addSuppressed(otherThrowable);
                    throw throwable;
                }
            }
        };
    }

    public static <T> ThrowingTest<T> fallback(ThrowingTest<T> action, ThrowingTest<T> other) {
        return t -> {
            try {
                return action.test(t);
            } catch (Throwable throwable) {
                try {
                    return other.test(t);
                } catch (Throwable otherThrowable) {
                    throwable.addSuppressed(otherThrowable);
                    throw throwable;
                }
            }
        };
    }

    public static <T> T rethrow(Throwable throwable) {
        return rethrow0(throwable);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R rethrow0(Throwable throwable) throws T {
        throw (T) throwable;
    }

    public static <T> ThrowingAccept<T> retrying(ThrowingAccept<T> action, Predicate<Throwable> shouldRetry, Consumer<Integer> beforeRetry, int maxRetries) {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicInteger retries = new AtomicInteger();
        return t -> {
            while (true) {
                try {
                    if (thrown.get() != null && beforeRetry != null)
                        beforeRetry.accept(retries.get());
                    action.accept(t);
                    return;
                } catch (Throwable throwable) {
                    if (!thrown.compareAndSet(null, throwable))
                        thrown.get().addSuppressed(throwable);
                    if ((shouldRetry != null && !shouldRetry.test(throwable)) || retries.getAndIncrement() == maxRetries)
                        break;
                }
            }
            throw thrown.get();
        };
    }

    public static <T, R> ThrowingApply<T, R> retrying(ThrowingApply<T, R> action, Predicate<Throwable> shouldRetry, Consumer<Integer> beforeRetry, int maxRetries) {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicInteger retries = new AtomicInteger();
        return t -> {
            while (true) {
                try {
                    if (thrown.get() != null && beforeRetry != null)
                        beforeRetry.accept(retries.get());
                    return action.apply(t);
                } catch (Throwable throwable) {
                    if (!thrown.compareAndSet(null, throwable))
                        thrown.get().addSuppressed(throwable);
                    if ((shouldRetry != null && !shouldRetry.test(throwable)) || retries.getAndIncrement() == maxRetries)
                        break;
                }
            }
            throw thrown.get();
        };
    }

    public static <T> ThrowingGet<T> retrying(ThrowingGet<T> action, Predicate<Throwable> shouldRetry, Consumer<Integer> beforeRetry, int maxRetries) {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicInteger retries = new AtomicInteger();
        return () -> {
            while (true) {
                try {
                    if (thrown.get() != null && beforeRetry != null)
                        beforeRetry.accept(retries.get());
                    return action.get();
                } catch (Throwable throwable) {
                    if (!thrown.compareAndSet(null, throwable))
                        thrown.get().addSuppressed(throwable);
                    if ((shouldRetry != null && !shouldRetry.test(throwable)) || retries.getAndIncrement() == maxRetries)
                        break;
                }
            }
            throw thrown.get();
        };
    }

    public static ThrowingRun retrying(ThrowingRun action, Predicate<Throwable> shouldRetry, Consumer<Integer> beforeRetry, int maxRetries) {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicInteger retries = new AtomicInteger();
        return () -> {
            while (true) {
                try {
                    if (thrown.get() != null && beforeRetry != null)
                        beforeRetry.accept(retries.get());
                    action.run();
                    return;
                } catch (Throwable throwable) {
                    if (!thrown.compareAndSet(null, throwable))
                        thrown.get().addSuppressed(throwable);
                    if ((shouldRetry != null && !shouldRetry.test(throwable)) || retries.getAndIncrement() == maxRetries)
                        break;
                }
            }
            throw thrown.get();
        };
    }

    public static <T> ThrowingTest<T> retrying(ThrowingTest<T> action, Predicate<Throwable> shouldRetry, Consumer<Integer> beforeRetry, int maxRetries) {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicInteger retries = new AtomicInteger();
        return t -> {
            while (true) {
                try {
                    if (thrown.get() != null && beforeRetry != null)
                        beforeRetry.accept(retries.get());
                    return action.test(t);
                } catch (Throwable throwable) {
                    if (!thrown.compareAndSet(null, throwable))
                        thrown.get().addSuppressed(throwable);
                    if ((shouldRetry != null && !shouldRetry.test(throwable)) || retries.getAndIncrement() == maxRetries)
                        break;
                }
            }
            throw thrown.get();
        };
    }

    @FunctionalInterface
    public interface ThrowingAccept<T> {

        void accept(T t) throws Throwable;

        default Consumer<T> toConsumer() {
            return toConsumer(Retries::rethrow);
        }

        default Consumer<T> toConsumer(Consumer<Throwable> handler) {
            return t -> {
                try {
                    accept(t);
                } catch (Throwable throwable) {
                    if (handler != null)
                        handler.accept(throwable);
                }
            };
        }

    }

    @FunctionalInterface
    public interface ThrowingApply<T, R> {

        R apply(T t) throws Throwable;

        default Function<T, R> toFunction() {
            return toFunction(Retries::rethrow);
        }

        default Function<T, R> toFunction(Consumer<Throwable> handler) {
            return t -> {
                try {
                    return apply(t);
                } catch (Throwable throwable) {
                    if (handler != null)
                        handler.accept(throwable);
                    return null;
                }
            };
        }

    }

    @FunctionalInterface
    public interface ThrowingGet<T> {

        T get() throws Throwable;

        default Supplier<T> toSupplier() {
            return toSupplier(Retries::rethrow);
        }

        default Supplier<T> toSupplier(Consumer<Throwable> handler) {
            return () -> {
                try {
                    return get();
                } catch (Throwable throwable) {
                    if (handler != null)
                        handler.accept(throwable);
                    return null;
                }
            };
        }

    }

    @FunctionalInterface
    public interface ThrowingRun {

        void run() throws Throwable;

        default Runnable toRunnable() {
            return toRunnable(Retries::rethrow);
        }

        default Runnable toRunnable(Consumer<Throwable> handler) {
            return () -> {
                try {
                    run();
                } catch (Throwable throwable) {
                    if (handler != null)
                        handler.accept(throwable);
                }
            };
        }

    }

    @FunctionalInterface
    public interface ThrowingTest<T> {

        boolean test(T t) throws Throwable;

        default Predicate<T> toPredicate() {
            return toPredicate(Retries::rethrow);
        }

        default Predicate<T> toPredicate(Consumer<Throwable> handler) {
            return t -> {
                try {
                    return test(t);
                } catch (Throwable throwable) {
                    if (handler != null)
                        handler.accept(throwable);
                    return false;
                }
            };
        }

    }

}
