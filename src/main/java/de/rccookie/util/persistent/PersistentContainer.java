package de.rccookie.util.persistent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.rccookie.util.Arguments;
import de.rccookie.util.Console;
import de.rccookie.util.Utils;
import de.rccookie.util.function.TriFunction;

public abstract class PersistentContainer<T> extends AbstractPersistentData<T> {

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Object dirtyLock = new Object();
    private final Object fsWriteLock = new Object();

    private final Path file;
    private final Path dir;

    private final Function<? super T, ? extends String> serializer;
    private final TriFunction<? super String, ? super T, ? super PersistentContainer<T>, ? extends T> deserializer;
    private final BiFunction<? super T, ? super PersistentContainer<T>, ? extends T> defaultStateApplier;
    protected T data;

    private boolean dirty = false;


    public PersistentContainer(Path file,
                               Function<? super PersistentContainer<T>, ? extends T> defaultGenerator,
                               Function<? super T, ? extends String> serializer,
                               BiFunction<? super String, ? super T, ? extends T> deserializer,
                               Function<? super T, ? extends T> defaultStateApplier) {
        this(file, defaultGenerator, serializer, (s,d,self) -> deserializer.apply(s,d), (d,self) -> defaultStateApplier.apply(d));
    }


    public PersistentContainer(Path file,
                               Function<? super PersistentContainer<T>, ? extends T> defaultGenerator,
                               Function<? super T, ? extends String> serializer,
                               TriFunction<? super String, ? super T, ? super PersistentContainer<T>, ? extends T> deserializer,
                               BiFunction<? super T, ? super PersistentContainer<T>, ? extends T> defaultStateApplier) {

        this.file = Arguments.checkNull(file, "file");
        this.dir = file.toAbsolutePath().normalize().getParent();
        this.serializer = Arguments.checkNull(serializer, "serializer");
        this.deserializer = Arguments.checkNull(deserializer, "deserializer");
        this.defaultStateApplier = Arguments.checkNull(defaultStateApplier, "defaultStateApplier");

        data = Arguments.checkNull(defaultGenerator, "defaultGenerator").apply(this);
        if(Files.exists(file)) try {
            data = deserialize(Files.readString(file));
        } catch(IOException e) {
            throw Utils.rethrow(e);
        }
        else applyDefaultState();

        new Writer(file).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(dirty)
                writeToDiskBlocking();
        }, file+" shutdown writer"));
    }


    @Override
    protected T data() {
        return data;
    }

    @Override
    public void markDirty() {
        synchronized(dirtyLock) {
            dirty = true;
            dirtyLock.notifyAll();
        }
    }

    @Override
    public void reload() {
        doWriteLocked(() -> {
            synchronized(fsWriteLock) {
                synchronized(dirtyLock) {
                    if(Files.exists(file)) try {
                        data = deserialize(Files.readString(file));
                    } catch(IOException e) {
                        throw Utils.rethrow(e);
                    }
                    else applyDefaultState();
                    dirty = false;
                }
            }
        });
    }

    private String serialize() {
        return serializer.apply(data);
    }

    private T deserialize(String str) {
        return deserializer.apply(str, data, this);
    }

    private void applyDefaultState() {
        data = defaultStateApplier.apply(data, this);
    }

    @Override
    public final ReadWriteLock lock() {
        return lock;
    }

    private void writeToDiskBlocking() {
        lock.readLock().lock();
        synchronized(fsWriteLock) {
            String str;
            try {
                synchronized(dirtyLock) {
                    str = serialize();
                    dirty = false;
                }
            } finally {
                lock.readLock().unlock();
            }
            try {
                Files.createDirectories(dir);
                Files.writeString(file, str);
            } catch(IOException e) {
                Console.error("Error writing data to disk:");
                Console.error(e);
            }
        }
    }



    private final class Writer extends Thread {

        public Writer(Path file) {
            super(file+" writer daemon");
            setDaemon(true);
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while(true) {
                synchronized(dirtyLock) {
                    while(!dirty) try {
                        dirtyLock.wait();
                    } catch(InterruptedException e) {
                        Console.error(e);
                    }
                }
                writeToDiskBlocking();
            }
        }
    }
}
