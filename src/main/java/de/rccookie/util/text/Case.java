package de.rccookie.util.text;

import de.rccookie.util.Arguments;

/**
 * A case formatting rule, such as camelCase, PascalCase or snake_case, and
 * utilities for translating between them.
 */
public interface Case {

    /**
     * Join all words in lowercase, using underscores as delimiters. Example:
     * <pre>hello_world</pre>
     */
    Case snake_case = withDelimiter("_", WordCase.ALL_LOW);
    /**
     * Join all words in uppercase, using underscores as delimiters. Example:
     * <pre>HELLO_WORLD</pre>
     */
    Case UPPER_SNAKE_CASE = withDelimiter("_", WordCase.ALL_UP);
    /**
     * Join all words in lowercase, using dashes as delimiters. Example:
     * <pre>hello-world</pre>
     */
    Case lower_dash_case = withDelimiter("-", WordCase.ALL_LOW);
    /**
     * Join all words in uppercase, using dashes as delimiters. Example:
     * <pre>HELLO-WORLD</pre>
     */
    Case UPPER_DASH_CASE = withDelimiter("-", WordCase.ALL_UP);
    /**
     * Synonym for {@link #lower_dash_case}.
     */
    Case kebab_case = lower_dash_case;
    /**
     * Synonym for {@link #UPPER_DASH_CASE}.
     */
    Case TRAIN_CASE = UPPER_DASH_CASE;
    /**
     * Join all words with each leading letter in uppercase, the other letters
     * in lowercase, using whitespaces as delimiters. Example:
     * <pre>Hello World</pre>
     */
    Case Title_Case = withDelimiter(" ", WordCase.FIRST_UP);
    /**
     * Join all words in lowercase, except the first word, whose first letter
     * should be uppercase, using whitespaces as delimiters. Example:
     * <pre>Hello world</pre>
     */
    Case Sentence = withDelimiter(" ", WordCase.FIRST_FIRST_UP);
    /**
     * Join all words with each letter being capitalized randomly, using
     * whitespaces as delimiters. Example:
     * <pre>hELlO worLd</pre>
     */
    Case stUdlY_cAps = withDelimiter(" ", WordCase.RANDOM);
    /**
     * Join all words, capitalizing each word's leading letter. Example:
     * <pre>HelloWorld</pre>
     */
    Case UpperCamelCase = new CapitalizedCase(WordCase.FIRST_UP);
    /**
     * Join all words, capitalizing each except the first word's leading
     * letter. Example:
     * <pre>helloWorld</pre>
     */
    Case lowerCamelCase = new CapitalizedCase(WordCase.FIRST_UP_EXCEPT_FIRST);
    /**
     * Synonym for {@link #lowerCamelCase}.
     */
    Case PascalCase = UpperCamelCase;
    /**
     * Synonym for {@link #UpperCamelCase}.
     */
    Case camelCase = lowerCamelCase;


    /**
     * Extracts the words from the given string formatted using this case formatting
     * rule. The capitalization of the words is retained (this is particularly relevant
     * for rules which can distinguish words using delimiters, without looking at the
     * words' cases, such as snake_case). Empty words are omitted.
     *
     * @param str The string formatted using this case rule
     * @return The words from the given string, separated
     */
    String[] split(String str);

    /**
     * Concatenates the given words into one string, capitalizing each word according
     * to this case rule and potentially inserting delimiters. The initial capitalization
     * of the words is discarded.
     *
     * @param words The words to join into a sentence
     * @return The words joined into one string according to this rule
     */
    String join(String... words);


    /**
     * Returns a modification of the given case rule which parses acronyms of technically
     * separate words into single words, or more specifically, joins multiple subsequent single
     * letter words into a single word. For example, parsing the string <code>ParseJSON</code>
     * according to PascalCase would result in the words <code>[ "Parse", "J", "S", "O", "N" ]</code>.
     * This case modification would convert that output to <code>[ "Parse", "JSON" ]</code>. Note
     * that when formatting back to PascalCase, the output now becomes <code>ParseJson</code>.
     * This modification is particularly helpful when converting a case which does not use
     * delimiters to one that does, e.g. PascalCase to snake_case. In that case, <code>ParseJSON</code>
     * would normally be transformed to <code>parse_j_s_o_n</code>, rather than <code>parse_json</code>.
     *
     * @param base The base formatting rules to use to split and join words
     * @return The modified case rule
     */
    static Case joiningAcronyms(Case base) {
        return base instanceof AcronymsJoiningCase ? base : new AcronymsJoiningCase(base);
    }

    /**
     * Returns a case rule which uses the specified (not empty!) delimiter to separate words.
     * Each word gets formatted according to the given word case rule.
     *
     * @param delimiter The delimiter between each pair of words, may not be empty
     * @param wordCase The case rule describing how to capitalize each word
     * @return A case from the given delimiter and word case
     */
    static Case withDelimiter(String delimiter, WordCase wordCase) {
        return new DelimitedCase(delimiter, wordCase);
    }

    /**
     * Translates the given string formatted according to the given current case rule
     * to the specified target case rule. This is equivalent to
     * <pre>targetCase.join(currentCase.split(str))</pre>.
     *
     * @param str The string to translate, formatted according to <code>currentCase</code>
     * @param currentCase The current casing of <code>str</code>
     * @param targetCase The case to translate the string to
     * @return The translated string
     */
    static String translate(String str, Case currentCase, Case targetCase) {
        return targetCase.join(currentCase.split(Arguments.checkNull(str, "str")));
    }

    /**
     * Translates the given string formatted according to the given current case rule
     * to the specified target case rule.
     *
     * @param str The string to translate, formatted according to <code>currentCase</code>
     * @param currentCase The current casing of <code>str</code>
     * @param targetCase The case to translate the string to
     * @param joinAcronyms If <code>false</code>, this method behaves exactly as
     *                     {@link #translate(String, Case, Case)}. If <code>true</code>,
     *                     <code>currentCase</code> will get wrapped using
     *                     {@link #joiningAcronyms(Case)} before translation.
     * @return The translated string
     */
    static String translate(String str, Case currentCase, Case targetCase, boolean joinAcronyms) {
        if(joinAcronyms)
            currentCase = joiningAcronyms(currentCase);
        return translate(str, currentCase, targetCase);
    }
}
