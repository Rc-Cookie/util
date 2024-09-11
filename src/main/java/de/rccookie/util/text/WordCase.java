package de.rccookie.util.text;

import java.util.stream.Collectors;

import de.rccookie.math.Mathf;

/**
 * A case rule for a single word; describes how to format a single word.
 */
public interface WordCase {

    /**
     * Case rule to format every word fully in lowercase.
     */
    WordCase ALL_LOW = (w, i) -> w.toLowerCase();
    /**
     * Case rule to format every word fully in uppercase.
     */
    WordCase ALL_UP = (w, i) -> w.toUpperCase();
    /**
     * Case rule to format the first letter of every word in uppercase, and the
     * other letters in lowercase.
     */
    WordCase FIRST_UP = (w,i) -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase();
    /**
     * Case rule to format the first letter of every word except the first in
     * uppercase and the other letters in lowercase, except the first word, which
     * gets formatted fully lowercase.
     */
    WordCase FIRST_UP_EXCEPT_FIRST = (w,i) -> (i == 0 ? ALL_LOW : FIRST_UP).apply(w,i);
    /**
     * Case rule to first the first letter of the first word in uppercase, and the
     * other letters of that word and all subsequent words in lowercase.
     */
    WordCase FIRST_FIRST_UP = (w,i) -> (i == 0 ? FIRST_UP : ALL_LOW).apply(w,i);
    /**
     * Case rule which capitalizes each letter randomly. Note that the PRG is not
     * seeded by the input in any way, which means formatting the same word multiple
     * times may yield different results.
     */
    WordCase RANDOM = (w,i) -> w.chars().mapToObj(c -> (Mathf.rand() < 0.5f ? Character.toLowerCase(c) : Character.toUpperCase(c)) + "").collect(Collectors.joining());

    /**
     * Formats the given word according to this rule. The initial case of the word
     * is discarded.
     *
     * @param word The word to format
     * @param index The index of the word in the whole string / sentence. Some rules may
     *              e.g. treat the first word differently.
     * @return The formatted word
     */
    String apply(String word, int index);
}
