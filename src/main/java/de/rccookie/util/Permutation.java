package de.rccookie.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import de.rccookie.json.JsonSerializable;

import org.jetbrains.annotations.NotNull;

public class Permutation implements Cloneable<Permutation>, Serializable, JsonSerializable {

    private final int[] permutation;

    public Permutation(int size) {
        permutation = new int[size];
        Arrays.setAll(permutation, IntUnaryOperator.identity());
    }
    public Permutation(int[] permutation) {
        this(permutation, true);
    }
    private Permutation(int[] permutation, boolean copyAndCheck) {
        if(copyAndCheck) {
            boolean[] contained = new boolean[permutation.length];
            for(int i=0; i<permutation.length; i++) {
                if(contained[Arguments.checkRange(permutation[i], 0, permutation.length)])
                    throw new IllegalArgumentException("Illegal permutation: duplicate image value "+permutation[i]);
                contained[i] = true;
            }
            this.permutation = permutation.clone();
        }
        else this.permutation = permutation;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Permutation && Arrays.equals(permutation, ((Permutation) obj).permutation));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(permutation);
    }

    @Override
    public String toString() {
        return Arrays.toString(permutation);
    }

    @Override
    public Object toJson() {
        return permutation;
    }

    @Override
    public @NotNull Permutation clone() {
        return new Permutation(permutation.clone(), false);
    }

    public int size() {
        return permutation.length;
    }

    public int map(int index) {
        return permutation[index];
    }

    public int[] toArray() {
        return permutation.clone();
    }

    public int sgn() {
        boolean[] visited = new boolean[permutation.length];
        int sgn = 1;
        while(true) {
            int start = 0;
            for(; start < visited.length && visited[start]; start++)
                start++;
            if(start == visited.length) break;
            int len = 1;
            visited[start] = true;
            for(int i=permutation[start]; i!=start; i=permutation[i], len++)
                visited[i] = true;
            if((len&1) == 0)
                sgn = -sgn;
        }
        return sgn;
    }


    public static Stream<Permutation> stream(int size) {
        Arguments.checkRange(size, 0, null);
        if(size == 0) return Stream.empty();

        Permutation p = new Permutation(size);
        int[] indices = new int[size];
        BoolWrapper first = new BoolWrapper(true);
        IntWrapper i = new IntWrapper();

        return Stream.iterate(p, $ -> {
            if(first.value) {
                first.value = false;
                return true;
            }
            while(i.value < size) {
                if(indices[i.value] < i.value) {
                    swap(p.permutation, i.value, (i.value&1) == 0 ? 0 : indices[i.value]);
                    indices[i.value]++;
                    i.value = 0;
                    return true;
                }
                else {
                    indices[i.value] = 0;
                    i.value++;
                }
            }
            return false;
        }, $ -> p);
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }


    public static void main(String[] args) {
        stream(12).forEach(System.out::println);
    }
}
