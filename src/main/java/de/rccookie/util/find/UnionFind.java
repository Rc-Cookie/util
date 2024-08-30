package de.rccookie.util.find;

import org.jetbrains.annotations.NotNull;

public final class UnionFind {

    private UnionFind() {
    }

    @NotNull
    public static Element makeSet() {
        return new Element();
    }

    @NotNull
    public static Element find(@NotNull Element x) {
        if(x.parent != x)
            x.parent = find(x.parent);
        return x.parent;
    }

    @NotNull
    public static Element union(@NotNull Element x, @NotNull Element y) {
        Element q = find(x), r = find(y);
        if(q.height > r.height) {
            r.parent = q;
            return q;
        }
        else {
            q.parent = r;
            if(q.height == r.height)
                r.height++;
            return r;
        }
    }

}
