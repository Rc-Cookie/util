package de.rccookie.util.find;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@NotNull
public class Element {
    @NotNull
    Element parent = this;
    @Range(from = 0)
    int height = 0;

    Element() {
    }
}
