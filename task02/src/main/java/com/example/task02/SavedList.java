package com.example.task02;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class SavedList<E extends Serializable> extends AbstractList<E> implements RandomAccess {
    private final Path path;
    private List<E> elements;

    public SavedList(File file) {
        path = file.toPath().toAbsolutePath();
        elements = load();
    }

    @Override
    public E get(int index) {
        return elements.get(index);
    }

    @Override
    public E set(int index, E element) {
        List<E> updated = new ArrayList<>(elements);
        E previous = updated.set(index, element);
        save(updated);
        return previous;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public void add(int index, E element) {
        List<E> updated = new ArrayList<>(elements);
        updated.add(index, element);
        save(updated);
        modCount++;
    }

    @Override
    public E remove(int index) {
        List<E> updated = new ArrayList<>(elements);
        E previous = updated.remove(index);
        save(updated);
        modCount++;
        return previous;
    }

    @SuppressWarnings("unchecked")
    private List<E> load() {
        try (var input = Files.newInputStream(path);
             var objects = new ObjectInputStream(input)) {
            Object saved = objects.readObject();
            if (!(saved instanceof List<?> list)) {
                throw new InvalidObjectException("Expected a saved list");
            }
            return new ArrayList<>((List<E>) list);
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("A saved element type is unavailable", e);
        }
    }

    private void save(List<E> updated) {
        try {
            Path temporary = Files.createTempFile(path.getParent(), "saved-list-", ".tmp");
            try {
                try (var output = Files.newOutputStream(temporary);
                     var objects = new ObjectOutputStream(output)) {
                    objects.writeObject(updated);
                }
                try {
                    Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
                }
                elements = updated;
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
