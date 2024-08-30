package de.rccookie.util;

import java.util.NavigableSet;
import java.util.TreeSet;

public class CompactIdGenerator {

    private final int firstId;
    private int nextId;
    private final NavigableSet<Integer> freeIds = new TreeSet<>();

    public CompactIdGenerator(int firstId) {
        this.firstId = firstId;
        nextId = firstId;
    }

    public CompactIdGenerator() {
        this(0);
    }

    public int allocate() {
        Integer id = freeIds.pollFirst();
        if(id == null)
            id = nextId++;
        return id;
    }

    public int peek() {
        Integer id = freeIds.pollFirst();
        if(id == null)
            id = nextId;
        return id;
    }

    public void free(int id) {
        Arguments.checkRange(id, firstId, null);
        if(nextId == id + 1) {
            nextId--;
            while(!freeIds.isEmpty() && freeIds.last() == nextId - 1) {
                freeIds.pollLast();
                nextId--;
            }
        }
        else freeIds.add(id);
    }

    public int usedCount() {
        return nextId - firstId - freeIds.size();
    }

    public int range() {
        return nextId - firstId;
    }

    public boolean isFree(int id) {
        Arguments.checkRange(id, firstId, null);
        return id >= nextId || freeIds.contains(id);
    }
}
