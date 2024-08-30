package de.rccookie.util;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import de.rccookie.json.JsonObject;
import de.rccookie.json.JsonSerializable;

public final class Options implements JsonSerializable {

    private final Map<String,String> options;
    private final String[] remaining;

    Options(Map<String,String> options, String[] remaining) {
        this.options = Collections.unmodifiableMap(options);
        this.remaining = remaining.clone();
    }

    public String get(String option) {
        return options.get(option);
    }

    public Optional<String> tryGet(String option) {
        return Optional.ofNullable(get(option));
    }

    public String getOr(String option, String alt) {
        String val = get(option);
        return val == null ? alt : val;
    }

    public String getOr(String option, Supplier<String> altGetter) {
        Arguments.checkNull(altGetter, "altGetter");
        String val = get(option);
        return val == null ? altGetter.get() : val;
    }

    public boolean is(String option) {
        return options.containsKey(option);
    }

    public Integer getInt(String option) {
        String val = get(option);
        return val == null ? null : Integer.parseInt(val);
    }

    public int getIntOr(String option, int alt) {
        String val = get(option);
        return val == null ? alt : Integer.parseInt(val);
    }

    public int getIntOr(String option, IntSupplier altGetter) {
        String val = get(option);
        return val == null ? altGetter.getAsInt() : Integer.parseInt(val);
    }

    public Long getLong(String option) {
        String val = get(option);
        return val == null ? null : Long.parseLong(val);
    }

    public long getLongOr(String option, long alt) {
        String val = get(option);
        return val == null ? alt : Long.parseLong(val);
    }

    public long getLongOr(String option, LongSupplier altGetter) {
        String val = get(option);
        return val == null ? altGetter.getAsLong() : Long.parseLong(val);
    }

    public Float getFloat(String option) {
        String val = get(option);
        return val == null ? null : Float.parseFloat(val);
    }

    public float getFloatOr(String option, float alt) {
        String val = get(option);
        return val == null ? alt : Float.parseFloat(val);
    }

    public float getFloatOr(String option, Supplier<Float> altGetter) {
        String val = get(option);
        return val == null ? altGetter.get() : Float.parseFloat(val);
    }

    public Double getDouble(String option) {
        String val = get(option);
        return val == null ? null : Double.parseDouble(val);
    }

    public double getDoubleOr(String option, double alt) {
        String val = get(option);
        return val == null ? alt : Double.parseDouble(val);
    }

    public double getDoubleOr(String option, DoubleSupplier altGetter) {
        String val = get(option);
        return val == null ? altGetter.getAsDouble() : Double.parseDouble(val);
    }



    public Map<String,String> getOptionsMap() {
        return options;
    }



    public String[] getArgs() {
        return remaining.clone();
    }

    public String getArgsString() {
        return String.join(" ", remaining);
    }

    public <T> T as(Class<T> type) {
        return getJson().strict(false).as(type);
    }

    @Override
    public Object toJson() {
        JsonObject json = new JsonObject(options);
        json.put("_remaining", remaining);
        return json;
    }
}
