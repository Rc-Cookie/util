package com.github.rccookie.common.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TagTable<TaggedType, TagType> {

    private final HashMap<TagType, Set<TaggedType>> tags = new HashMap<>();

    public final boolean tag(TaggedType object, TagType tag) {
        if(object == null || tag == null) return false;

        if(tags.get(tag) == null) tags.put(tag, new HashSet<>());

        return tags.get(tag).add(object);
    }

    public final boolean untag(TaggedType object, TagType tag) {
        if(object == null || tag == null) return false;

        if(tags.get(tag) == null) return false;

        return tags.get(tag).remove(object);
    }


    public final Set<TaggedType> getTagged(TagType tag) {
        return new HashSet<>(tags.get(tag));
    }


    public final Set<TagType> getTags(TaggedType object) {
        Set<TagType> out = new HashSet<>();

        for(Map.Entry<TagType, Set<TaggedType>> entry : tags.entrySet()) {
            if(entry.getValue().contains(object)) out.add(entry.getKey());
        }

        return out;
    }

    public boolean isTagged(TaggedType object, TagType tag) {
        return getTags(object).contains(tag);
    }



    public TagTable() {}

    public static void main(String[] args) {
        
    }
}
