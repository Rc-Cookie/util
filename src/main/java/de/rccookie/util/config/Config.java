package de.rccookie.util.config;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.rccookie.json.Json;
import de.rccookie.json.JsonArray;
import de.rccookie.json.JsonElement;
import de.rccookie.json.JsonObject;
import de.rccookie.json.JsonParseException;
import de.rccookie.util.Arguments;
import de.rccookie.util.Console;
import de.rccookie.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config { // TODO: only save when changed, remove from file if set to default, thread-safety, store instances in map and reuse

    @Nullable
    private JsonObject data;
    @NotNull
    private String path;

    @NotNull
    private JsonObject defaults = new JsonObject();

    public Config(@NotNull String path) {
        this.path = Arguments.checkNull(path, "path");
    }

    @NotNull
    public String getPath() {
        return path;
    }

    public void setPath(@NotNull String path) {
        this.path = Arguments.checkNull(path, "path");
        data = null;
    }

    public void setAppdataPath(@NotNull String name) {
        setPath(Utils.getAppdata() + "/" + Arguments.checkNull(name, "name") + "/settings.json");
    }

    public void setDefaults(@NotNull JsonObject defaults) {
        this.defaults = new JsonObject(Arguments.checkNull(defaults, "defaults"));
    }

    public void setDefaults(@NotNull String path) {
        setDefaults(Json.load(Arguments.checkNull(path, "path")).asObject());
    }

    public void setDefaults(@NotNull Object defaults) {
        setDefaults(JsonElement.wrap(Arguments.checkNull(defaults, "defaults")).asObject());
    }

    @NotNull
    public JsonObject getDefaults() {
        return new JsonObject(defaults);
    }

    public void clearDefaults() {
        this.defaults = new JsonObject();
    }



    public boolean set(String setting, Object value) {
        List<Object> path = parsePath(Arguments.checkNull(setting, "setting"));
        value = Json.serialize(value);

        Object cur = data();
        for(int i=0; i<path.size()-1; i++) {
            Object key = path.get(i);
            if(key instanceof String) {
                int _i = i;
                cur = ((JsonObject) cur).computeIfAbsent((String) key, k -> path.get(_i + 1) instanceof String ? new JsonObject() : new JsonArray());
            }
            else {
                assert cur instanceof JsonArray;
                JsonArray arr = (JsonArray) cur;
                int index = (int) key;
                while(arr.size() < index+1)
                    arr.add(null);
                cur = arr.get(index);
                if(cur == null)
                    cur = path.get(i+1) instanceof String ? new JsonObject() : new JsonArray();
            }
        }
        Object key = path.get(path.size()-1);
        if(key instanceof String)
            ((JsonObject) cur).put((String) key, value);
        else {
            JsonArray arr = (JsonArray) cur;
            int index = (int) key;
            while(arr.size() < index+1)
                arr.add(null);
            arr.set(index, value);
        }

        new File(this.path).getParentFile().mkdir();
        return data().store(new File(this.path));
    }


    public JsonElement getElement(String setting) {
        Object[] path = parsePath(setting).toArray();
        JsonElement element = data().getPath(path);
        return element.isPresent() ? element : defaults.getPath(path);
    }

    public JsonElement getSetElement(String setting) {
        return data().getPath(parsePath(setting));
    }

    private JsonElement element(String setting) {
        return getElement(setting);
    }

    public boolean isSet(String setting) {
        return data().getPath(setting).isPresent();
    }

    public boolean isSetOrDefault(String setting) {
        return getElement(setting).isPresent();
    }

    public Object get(String setting) {
        return getElement(setting).get();
    }

    public <T> T get(String setting, Class<T> type) {
        return element(setting).as(type);
    }

    public String getString(String setting) {
        return element(setting).asString();
    }

    public List<Object> getList(String setting) {
        return Utils.view(element(setting).asArray());
    }

    public Map<String,Object> asMap(String setting) {
        return Utils.view(element(setting).asObject());
    }

    public Integer getInt(String setting) {
        return element(setting).asInt();
    }

    public Long getLong(String setting) {
        return element(setting).asLong();
    }

    public Float getFloat(String setting) {
        return element(setting).asFloat();
    }

    public Double getDouble(String setting) {
        return element(setting).asDouble();
    }

    public Boolean getBool(String setting) {
        return element(setting).asBool();
    }



    public void reload() {
        data = null;
    }



    @NotNull
    protected JsonObject data() {
        if(data == null) {
            try {
                data = Json.load(path).orGet(JsonObject.class, JsonObject::new);
            } catch(UncheckedIOException e) {
                data = new JsonObject();
            } catch(JsonParseException|ClassCastException e) {
                Console.warn("Corrupted settings file: " + e);
                data = new JsonObject();
            }
        }
        return data;
    }



    @NotNull
    public static Config fromAppdataPath(@NotNull String name) {
        Config c = new Config("");
        c.setAppdataPath(name);
        return c;
    }



    private static List<Object> parsePath(String setting) {
        if(setting.isEmpty())
            throw new IllegalArgumentException("Empty path");
        if(setting.charAt(setting.length()-1) == '.')
            throw new IllegalArgumentException("Empty path element");

        List<Object> path = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for(int p=0; p<setting.length(); p++) {
            char c = setting.charAt(p);
            if(c == ']') {
                path.add(Integer.parseInt(cur.toString()));
                cur.delete(0, cur.length());
                if(p+1 != setting.length() && ((c = setting.charAt(p+1)) == '[' || c == '.')) p++;
            }
            else if(c == '.' || c == '[') {
                if(cur.length() == 0)
                    throw new IllegalArgumentException("Empty path element");
                path.add(cur.toString());
                cur.delete(0, cur.length());
            }
            else if(c == '\\') {
                if(++p == setting.length())
                    break;
                cur.append(setting.charAt(p));
            }
            else cur.append(c);
        }
        if(cur.length() != 0)
            path.add(cur.toString());

        return path;
    }
}
