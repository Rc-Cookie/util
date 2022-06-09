package com.github.rccookie.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class ModIterableList<E> implements List<E> {

    int size = 0;
    Node head = null;
    Node tail = null;

    Iterator iterator = null;

    public ModIterableList() {
    }

    public ModIterableList(Collection<? extends E> c) {
        addAll(c);
    }

    @Override
    public String toString() {
        if(head == null) return "[]";
        StringBuilder str = new StringBuilder("[");
        for(Node n = head; n != null; n = n.next)
            str.append(n.value).append(", ");
        str.replace(str.length() - 2, str.length(), "]");
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof List<?>)) return false;
        if(size != ((List<?>) o).size()) return false;

        java.util.Iterator<?> it = ((List<?>) o).iterator();
        for(Node n = head; n != null; n = n.next)
            if(!it.hasNext() || !Objects.equals(n.value, it.next())) return false;
        return !it.hasNext();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for(Node n = head; n != null; n = n.next)
            hash = 13 * hash + Objects.hashCode(n.value);
        return hash;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean contains(Object o) {
        for(Node n = head; n != null; n = n.next)
            if(Objects.equals(n.value, o)) return true;
        return false;
    }

    @NotNull
    @Override
    public java.util.Iterator<E> iterator() {
        return iterator = new Iterator(head, iterator, false);
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return toArray(new Object[size]);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> T @NotNull [] toArray(T @NotNull[] a) {
        if(a.length < size)
            a = Arrays.copyOf(a, size);
        int i = 0;
        for(Node n = head; n != null; n = n.next, i++)
            a[i] = (T) n.value;
        return a;
    }

    @Override
    public boolean add(E e) {
        Node n = new Node(e, tail, null);
        if(tail != null)
            tail.next = n;
        else head = n;
        tail = n;
        size++;
        addedTail();
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for(Node n = head; n != null; n = n.next) {
            if(Objects.equals(n.value, o)) {
                if(n.prev == null) head = n.next;
                else n.prev.next = n.next;
                if(n.next == null) tail = n.prev;
                else n.next.prev = n.prev;
                size--;
                removed(n);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for(Object o : c)
            if(!contains(o)) return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        if(c.isEmpty()) return false;
        for(E e : c) add(e);
        return true;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        if(c.isEmpty()) return false;
        Node h = new Node(null, null, null);
        Node t = h;
        for(E e : c) {
            t = new Node(e, t, null);
            t.prev.next = h;
            size++;
        }
        Node n = head;
        for(int i=0; i<index; i++)
            n = n.next;

        if(n == null) {
            h.next.prev = tail;
            if(tail == null) head = h.next;
            else tail.next = h.next;
            tail = h.next;
            for(Iterator it = iterator; it != null; it = it.next)
                if(it.node == null) it.node = h.next;
        }
        else {
            h.next.prev = n.prev;
            if(n.prev == null) head = h.next;
            else n.prev.next = h.next;
        }

        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean change = false;
        for(Object o : c)
            change |= remove(o);
        return change;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        java.util.Iterator<E> it = iterator();
        boolean change = false;
        while(it.hasNext())
            if(change |= !c.contains(it.next()))
                it.remove();
        return change;
    }

    @Override
    public void clear() {
        head = tail = null;
        size = 0;
        for(Iterator it = iterator, prev = null; it != null; prev = it, it = it.next) {
            it.node = null;
            if(!it.reversible) {
                if(prev == null) iterator = it.next;
                else prev.next = it.next;
            }
        }
    }

    @Override
    public E get(int index) {
        Node n = head;
        for(;index > 0; index--) n = n.next;
        return n.value;
    }

    @Override
    public E set(int index, E element) {
        Node n = head;
        for(;index > 0; index--) n = n.next;
        E old = n.value;
        n.value = element;
        return old;
    }

    @Override
    public void add(int index, E element) {
        Node n = head;
        for(;index > 0; index--) n = n.next;
        if(n == null) {
            tail = new Node(element, tail, null);
            if(tail.prev == null) head = tail;
            else tail.prev.next = tail;
            addedTail();
        }
        else {
            n.prev = new Node(element, n.prev, n);
            if(n.prev.prev == null) head = n.prev;
            else n.prev.prev.next = n.prev;
        }
        size++;
    }

    @Override
    public E remove(int index) {
        Node n = head;
        for(;index > 0; index--) n = n.next;
        if(n.prev == null) head = n.next;
        else n.prev.next = n.next;
        if(n.next == null) tail = n.prev;
        else n.next.prev = n.prev;
        size--;
        removed(n);
        return n.value;
    }

    @Override
    public int indexOf(Object o) {
        int i = 0;
        for(Node n = head; n != null; n = n.next, i++)
            if(Objects.equals(n.value, n)) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int i = size - 1;
        for(Node n = tail; n != null; n = n.prev, i--)
            if(Objects.equals(n.value, n)) return i;
        return -1;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        if(index < 0 || index > size) throw new IndexOutOfBoundsException(index + " too " + (index < 0 ? "low" : "high"));
        Node n = head;
        for(;index > 0; index--) n = n.next;
        return iterator = new Iterator(n, iterator, true);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private void addedTail() {
        for(Iterator it = iterator; it != null; it = it.next)
            if(it.node == null) it.node = tail;
    }

    private void removed(Node n) {
        for(Iterator it = iterator, prev = null; it != null; prev = it, it = it.next) {
            if(it.node == n) {
                it.node = it.node.prev;
                if(!it.reversible && it.node == null) {
                    if(prev == null) iterator = it.next;
                    else prev.next = it.next;
                }
            }
        }
    }

    private class Node {
        E value;
        Node prev;
        Node next;
        Node(E value, Node prev, Node next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }
    }

    private class Iterator implements ListIterator<E> {

        Node node;
        boolean lastWasNext = true;
        final boolean reversible;

        Iterator next;

        Iterator(Node node, Iterator next, boolean reversible) {
            this.node = node;
            this.next = next;
            this.reversible = reversible;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public E next() {
            if(node == null) throw new NoSuchElementException();
            E value = node.value;
            node = node.next;
            lastWasNext = true;
            return value;
        }

        @Override
        public boolean hasPrevious() {
            return (node == null && tail != null) || (node != null && node.prev != null);
        }

        @Override
        public E previous() {
            if((node != null || tail == null) && (node == null || node.prev == null)) throw new NoSuchElementException();
            node = node == null ? tail : node.prev;
            lastWasNext = false;
            return node.value;
        }

        @Override
        public int nextIndex() {
            int i = 0;
            for(Node n = head; n != node; n = n.next) i++;
            return i;
        }

        @Override
        public int previousIndex() {
            return nextIndex() - 1;
        }

        @Override
        public void remove() {
            Node n = lastWasNext ? node == null ? tail : node.prev : node;
            if(n.prev == null) head = n.next;
            else n.prev.next = n.next;
            if(n.next == null) tail = n.prev;
            else n.next.prev = n.prev;
            size--;
            removed(n);
        }

        @Override
        public void set(E e) {
            Node n = lastWasNext ? node == null ? tail : node.prev : node;
            n.value = e;
        }

        @Override
        public void add(E e) {
            Node prev = node == null ? tail : node.prev;
            Node n = new Node(e, prev, node);
            if(prev == null) head = n;
            else prev.next = n;
            if(node == null) tail = n;
            else node.prev = n;
            size++;
            node = n;
            if(tail == n) addedTail();
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ModIterableArrayList<>();
        for(int i=0; i<10; i++) list.add(i);
        Console.log(list);
        list.forEach(list::remove);
        Console.log(list);
    }
}
