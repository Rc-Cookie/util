package de.rccookie.util.text;

import java.util.ArrayList;
import java.util.List;

class CapitalizedCase extends AbstractCase {

    CapitalizedCase(WordCase wordCase) {
        super("", wordCase);
    }

    @Override
    public String[] split(String str) {
        List<String> words = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for(char c : str.toCharArray()) {
            if(Character.isUpperCase(c) && current.length() != 0) {
                words.add(current.toString());
                current.setLength(0);
            }
            current.append(c);
        }
        if(current.length() != 0)
            words.add(current.toString());
        return words.toArray(new String[0]);
    }
}
