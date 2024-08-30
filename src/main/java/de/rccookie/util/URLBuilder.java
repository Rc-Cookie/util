package de.rccookie.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class URLBuilder {

    private String protocol;
    private String host;
    private int port;
    private String path;
    private final Map<String,String> query = new HashMap<>();
    private String ref;

    public URLBuilder(String protocol, String host) {
        this.protocol = Arguments.checkNull(protocol, "protocol");
        this.host = Arguments.checkNull(host, "host");
        port = -1;
        path = "";
        ref = null;
    }

    public URLBuilder(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public URLBuilder(URL url) {
        protocol = url.getProtocol();
        host = Objects.requireNonNull(url.getHost());
        port = url.getPort();
        path = url.getPath();
        queryString(url.getQuery());
        ref = url.getRef() != null ? URLDecoder.decode(url.getRef(), StandardCharsets.UTF_8) : null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(protocol).append("://");
        str.append(host);
        if(port != -1)
            str.append(':').append(port);
        str.append(path);
        if(!query.isEmpty())
            str.append('?').append(queryString());
        if(ref != null)
            str.append('#').append(URLEncoder.encode(ref, StandardCharsets.UTF_8));
        return str.toString();
    }

    public URL toURL() throws MalformedURLException {
        return new URL(toString());
    }

    @NotNull
    public String protocol() {
        return protocol;
    }

    public URLBuilder protocol(@NotNull String protocol) {
        this.protocol = Arguments.checkNull(protocol, "protocol");
        return this;
    }

    @NotNull
    public String authority() {
        return port >= 0 ? host + ":" + port : host;
    }

    @NotNull
    public String host() {
        return host;
    }

    @Range(from = -1, to = 65535)
    public int port() {
        return port;
    }

    public URLBuilder authority(@NotNull String authority) {
        Arguments.checkNull(authority, "authority");
        String host;
        int port = -1;
        if(authority.startsWith("[")) {
            int end = authority.indexOf(']');
            host = authority.substring(0, end);
            if(end+1 != authority.length()) {
                if(authority.charAt(end+1) != ':')
                    throw new IllegalArgumentException("Illegal authority, port or nothing expected after IPv6 address: "+authority);
                if(end+2 != authority.length()) {
                    port = Integer.parseInt(authority.substring(end + 2));
                    if(port < 0 || port > 65535)
                        throw new ArgumentOutOfRangeException("Port "+port);
                }
            }
        }
        else if(authority.contains(":")) {
            int index = authority.indexOf(':');
            if(index != authority.length()-1) {
                host = authority.substring(0, index);
                port = Integer.parseInt(authority.substring(index+1));
                if(port < 0 || port > 65535)
                    throw new ArgumentOutOfRangeException("Port "+port);
            }
            else host = authority;
        }
        else host = authority;
        this.host = host;
        this.port = port;
        return this;
    }

    public URLBuilder host(@NotNull String host) {
        return authority(Arguments.checkNull(host, "host"));
    }

    public URLBuilder port(@Range(from = -1, to = 65535) int port) {
        this.port = Arguments.checkRange(port, -1, 1<<16);
        return this;
    }

    @NotNull
    public String path() {
        return path;
    }

    public URLBuilder path(@Nullable String path) {
        this.path = path != null ? path.startsWith("/") ? path : "/"+path : "/";
        return this;
    }

    @Nullable
    public Map<String, String> query() {
        return Utils.view(query);
    }

    @NotNull
    public String queryString() {
        return queryString(query);
    }

    public URLBuilder query(Map<? extends String, ? extends String> query) {
        Arguments.checkNull(query, "query");
        this.query.clear();
        this.query.putAll(query);
        return this;
    }

    public URLBuilder param(@NotNull String name) {
        return param(name, null);
    }

    public URLBuilder param(@NotNull String name, @Nullable String value) {
        query.put(name, value);
        return this;
    }

    public URLBuilder addParams(@NotNull Map<? extends String, ? extends String> params) {
        query.putAll(Arguments.checkNull(params, "params"));
        return this;
    }

    public URLBuilder setParams(@NotNull Map<? extends String, ? extends String> params) {
        Arguments.checkNull(params, "params");
        query.clear();
        query.putAll(params);
        return this;
    }

    public URLBuilder clearParam(@NotNull String name) {
        query.remove(name);
        return this;
    }

    public URLBuilder clearParams() {
        query.clear();
        return this;
    }

    public URLBuilder queryString(String query) {
        if(query == null) {
            this.query.clear();
            return this;
        }
        Map<String, String> params = new HashMap<>();
        for(String param : query.split("&")) {
            int index = param.indexOf('=');
            if(index == -1)
                params.put(URLDecoder.decode(param, StandardCharsets.UTF_8), null);
            else params.put(URLDecoder.decode(param.substring(0, index), StandardCharsets.UTF_8), URLDecoder.decode(param.substring(index+1), StandardCharsets.UTF_8));
        }
        this.query.clear();
        this.query.putAll(params);
        return this;
    }

    @Nullable
    public String ref() {
        return ref;
    }

    public URLBuilder ref(@Nullable String ref) {
        this.ref = ref == null || ref.isBlank() ? null : ref;
        return this;
    }



    public static String queryString(Map<? extends String, ? extends String> query) {
        if(query.isEmpty()) return "";
        StringBuilder str = new StringBuilder();
        query.forEach((k,v) -> {
            str.append(URLEncoder.encode(k, StandardCharsets.UTF_8));
            if(v != null)
                str.append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            str.append('&');
        });
        return str.deleteCharAt(str.length()-1).toString();
    }
}
