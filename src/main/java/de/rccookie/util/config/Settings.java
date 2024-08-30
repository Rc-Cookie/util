package de.rccookie.util.config;

import java.util.List;
import java.util.Map;

import de.rccookie.json.Json;
import de.rccookie.json.JsonElement;
import de.rccookie.json.JsonObject;
import de.rccookie.util.Console;

public final class Settings {

    private Settings() { }



    private static final Config data = Config.fromAppdataPath("Settings");



    public static JsonObject getDefaults() {
        return data.getDefaults();
    }

    public static void addDefaults(JsonObject defaults) {
        JsonObject combined = data.getDefaults();
        combined.combine(defaults);
        data.setDefaults(combined);
    }



    public static boolean set(String setting, Object value) {
        return data.set(setting, value);
    }



    public static JsonElement getElement(String setting) {
        return data.getElement(setting);
    }

    public static JsonElement getSetElement(String setting) {
        return data.getSetElement(setting);
    }

    public static boolean isSet(String setting) {
        return data.isSet(setting);
    }

    public static boolean isSetOrDefault(String setting) {
        return data.isSetOrDefault(setting);
    }

    public static Object get(String setting) {
        return data.get(setting);
    }

    public static <T> T get(String setting, Class<T> type) {
        return data.get(setting, type);
    }

    public static String getString(String setting) {
        return data.getString(setting);
    }

    public static List<Object> getList(String setting) {
        return data.getList(setting);
    }

    public static Map<String,Object> asMap(String setting) {
        return data.asMap(setting);
    }

    public static Integer getInt(String setting) {
        return data.getInt(setting);
    }

    public static Long getLong(String setting) {
        return data.getLong(setting);
    }

    public static Float getFloat(String setting) {
        return data.getFloat(setting);
    }

    public static Double getDouble(String setting) {
        return data.getDouble(setting);
    }

    public static Boolean getBool(String setting) {
        return data.getBool(setting);
    }



    public void reload() {
        data.reload();
    }



    public static void main(String[] args) {
        while(true) {
            String[] in = Console.input(">").split("=");
            if(in.length == 1)
                Console.log(get(in[0]));
            else
                set(in[0].trim(), Json.parse(in[1] + " "));
        }
    }
}
