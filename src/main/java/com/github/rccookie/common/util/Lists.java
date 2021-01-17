package com.github.rccookie.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Lists {

    /**
     * Returns only those entries of the list which satisfy the given requirement in initial
     * order.
     * 
     * @param <T> The type in the list
     * @param list The list
     * @param requirement The requirement for list entries to fullfill
     * @return A new list with only those entries of the list which satisfy the requirement
     */
    public static final <T> List<T> only(List<T> list, Predicate<T> requirement) {
        List<T> out = new ArrayList<>();
        for(T t : list) if(requirement.test(t)) out.add(t);
        return out;
    }

    /**
     * Returns one entry of the list which satisfies the given requirement. If no entries
     * satisfy the requirement, {@code null} will be returned.
     * 
     * @param <T> The type in the list
     * @param list The list
     * @param requirement The requirement for list entries to fullfill
     * @return An entry of the list which satisfies the requirement, or {@code null}
     */
    public static final <T> T one(List<T> list, Predicate<T> requirement) {
        for(T t : list) if(requirement.test(t)) return t;
        return null;
    }
    
    /**
     * Returns weather any of the entries of the list satisfy the given requirement.
     * 
     * @param <T> The type in the list
     * @param list The list
     * @param requirement The requirement for list entries to fullfill
     * @return {@code true} if at least one entry if the list satisfies the given requirement
     */
    public static final <T> boolean any(List<T> list, Predicate<T> requirement) {
        return one(list, requirement) != null;
    }

    /**
     * Preforms the given action only on those entries of the list which satisfy the requirement.
     * 
     * @param <T> The type in the list
     * @param list The list
     * @param requirement The requirement for list entries to fullfill
     * @param action The action to perform on some list entries
     */
    public static final <T> void some(List<T> list, Predicate<T> requirement, Consumer<T> action) {
        for(T t : only(list, requirement)) action.accept(t);
    }

    /**
     * Returns how many of the list entries satisfy the given requirement.
     * 
     * @param <T> The type in the list
     * @param list The list
     * @param requirement The requirement for list entries to fullfill
     * @return The number of entries in the list that satisfy the requirement
     */
    public static final <T> int count(List<T> list, Predicate<T> requirement) {
        return only(list, requirement).size();
    }


    private Lists() {}
}
