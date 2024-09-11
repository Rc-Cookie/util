package de.rccookie.util.text;

import java.util.ArrayList;
import java.util.List;

import de.rccookie.util.Arguments;

class AcronymsJoiningCase implements Case {

    private final Case base;

    AcronymsJoiningCase(Case base) {
        this.base = Arguments.checkNull(base, "base");
    }

    @Override
    public String[] split(String str) {
        return joinAbbreviations(base.split(str));
    }

    @Override
    public String join(String... words) {
        return base.join(words);
    }

    private String[] joinAbbreviations(String[] words) {
        List<String> joined = new ArrayList<>(words.length);
        boolean lastWasSingle = false;
        for(String w : words) {
            if(w.isEmpty())
                continue;
            if(w.length() == 1 && lastWasSingle) {
                int i = joined.size() - 1;
                joined.set(i, joined.get(i) + w);
            }
            else joined.add(w);
            lastWasSingle = w.length() == 1;
        }
        return joined.size() == words.length ? words : joined.toArray(new String[0]);
    }
}
