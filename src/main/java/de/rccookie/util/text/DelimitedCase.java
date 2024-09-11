package de.rccookie.util.text;

import java.util.ArrayList;
import java.util.List;

class DelimitedCase extends AbstractCase {

    DelimitedCase(String delimiter, WordCase wordCase) {
        super(delimiter, wordCase);
        if(delimiter.isEmpty())
            throw new IllegalArgumentException("Delimiter may not be empty");
    }

    @Override
    public String[] split(String str) {
        List<String> words = new ArrayList<>();
        int lastEnd = 0;
        int index = str.indexOf(delimiter);
        while(index >= 0) {
            if(index != lastEnd)
                words.add(str.substring(lastEnd, index));
            lastEnd = index + delimiter.length();
            index = str.indexOf(delimiter, lastEnd);
        }
        if(lastEnd < str.length())
            words.add(str.substring(lastEnd));
        return words.toArray(new String[0]);
    }
}
