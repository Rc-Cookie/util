package de.rccookie.util.text;

import de.rccookie.util.Arguments;

abstract class AbstractCase implements Case {

    protected final String delimiter;
    protected final WordCase wordCase;

    AbstractCase(String delimiter, WordCase wordCase) {
        this.delimiter = Arguments.checkNull(delimiter, "delimiter");
        this.wordCase = Arguments.checkNull(wordCase, "wordCase");
    }

    @Override
    public String join(String... words) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        for(String word : words) {
            if(word.isEmpty())
                continue;
            if(i != 0)
                str.append(delimiter);
            str.append(wordCase.apply(word, i++));
        }
        return str.toString();
    }
}
