package com.github.rccookie.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.rccookie.json.Json;
import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Future;
import com.github.rccookie.util.FutureImpl;
import com.github.rccookie.util.ThreadedFutureImpl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An unsent HTTP request. HTTP requests can be safely reused.
 */
public class HTTPRequest {

    /**
     * The url for the request.
     */
    private final String url;
    /**
     * The request method.
     */
    private Method method = Method.GET;
    /**
     * Custom header fields for the request.
     */
    private final Map<String,String> header = new HashMap<>();
    /**
     * View of {@link #header}.
     */
    private final Map<String,String> headerView = Collections.unmodifiableMap(header);
    /**
     * Whether redirects are allowed.
     */
    private boolean redirects = true;


    /**
     * Creates a new HTTP request for the specified url.
     *
     * @param url The url to send the request to
     */
    public HTTPRequest(String url) {
        if(!Arguments.checkNull(url, "url").startsWith("http"))
            throw new IllegalArgumentException("HTTP request urls must start with http or https");
        this.url = url;
    }

    /**
     * Returns the HTTP request url.
     *
     * @return The url used
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns a view of the custom header fields in this HTTP request.
     *
     * @return The header fields
     */
    public Map<String, String> getHeader() {
        return headerView;
    }

    /**
     * Returns the request method, which is {@link Method#GET} by default.
     *
     * @return The request method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns whether automatic redirects are allowed.
     *
     * @return Whether redirects are allowed
     */
    public boolean isAllowRedirects() {
        return redirects;
    }

    /**
     * Sets the request method for the HTTP request. The default method is
     * {@link Method#GET}.
     *
     * @param method The method to use
     * @return This HTTP request
     */
    public HTTPRequest setMethod(@NotNull Method method) {
        this.method = Arguments.checkNull(method, "method");
        return this;
    }

    /**
     * Sets the specified header field to the given value.
     *
     * @param key The header field to set
     * @param value The value of that header field
     * @return This HTTP request
     */
    public HTTPRequest setHeaderField(@NotNull String key, @NotNull String value) {
        header.put(Arguments.checkNull(key, "key"), Arguments.checkNull(value, "value"));
        return this;
    }

    /**
     * Sets the {@code "Content-Type"} header field.
     *
     * @param contentType The content type of this HTTP request
     * @return This HTTP request
     */
    public HTTPRequest setContentType(String contentType) {
        return setHeaderField("Content-Type", contentType);
    }

    /**
     * Sets the {@code "Cookie"} header field. Cookies must be seperated with {@code "; "}.
     *
     * @param cookies The cookies to use
     * @return This HTTP request
     */
    public HTTPRequest setCookies(String cookies) {
        return setHeaderField("Cookie", cookies);
    }

    /**
     * Sets whether automatic redirects should be allowed. This is on by default.
     *
     * @param allow Whether redirects should be allowed
     * @return This HTTP request
     */
    public HTTPRequest allowRedirects(boolean allow) {
        this.redirects = allow;
        return this;
    }

    /**
     * Sends this HTTP request asynchronously.
     *
     * @param data The data to send.
     * @return A future to the HTTP response
     */
    public Future<HTTPResponse> send(@Nullable String data) {
        return sendRequest(url, data);
    }

    /**
     * Sends this HTTP request asynchronously without content.
     *
     * @return A future to the HTTP response
     */
    public Future<HTTPResponse> send() {
        return send(null);
    }

    /**
     * Sends this HTTP request asynchronously with the given data converted to json format.
     *
     * @param json The object to be sent as json. Must be convertable to json
     * @return A future to the HTTP response
     */
    public Future<HTTPResponse> sendJson(Object json) {
        return send(Json.toString(json));
    }

    /**
     * Sends this HTTP request asynchronously with the given parameters appended on the url.
     * The url must not include any parameters before.
     *
     * @param params Parameters to be included in the url of the request to send, converted to
     *               strings using their respective toString() method
     * @return A future to the HTTP response
     */
    public Future<HTTPResponse> sendParams(Map<?,?> params) {
        String paramsString = "?" + params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        return sendRequest(url + paramsString, null);
    }

    /**
     * Returns the request method and the url as string.
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return method + " " + url;
    }

    private Future<HTTPResponse> sendRequest(String url, String data) {
        FutureImpl<HTTPResponse> result = new ThreadedFutureImpl<>();
        new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod(method.toString());
                con.setInstanceFollowRedirects(redirects);
                header.forEach(con::setRequestProperty);

                if(data != null) {
                    con.setDoOutput(true);
                    con.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
                }

                int code = con.getResponseCode();
                InputStream in = code < 400 ? con.getInputStream() : con.getErrorStream();
                ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                for (int length; (length = in.read(buffer)) != -1; ) {
                    resultStream.write(buffer, 0, length);
                }
                String responseData = resultStream.toString(StandardCharsets.UTF_8);
                Map<String, List<String>> resultHeader = con.getHeaderFields();
                Map<String, String> stringHeaders = new HashMap<>(resultHeader.size());
                resultHeader.forEach((k, vs) -> stringHeaders.put(k, String.join(";", vs)));

                result.complete(new HTTPResponse(code, resultStream.toByteArray(), responseData, stringHeaders));
            } catch (IOException e) {
                result.fail(e);
            }
        }).start();
        return result;
    }

    /**
     * HTTP request methods.
     *
     * <p><a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">Reference</a></p>
     */
    public enum Method {
        /**
         * The GET method requests a representation of the specified resource. Requests using GET should only retrieve data.
         */
        GET,
        /**
         * The HEAD method asks for a response identical to a GET request, but without the response body.
         */
        POST,
        /**
         * The PUT method replaces all current representations of the target resource with the request payload.
         */
        PUT,
        /**
         * The HEAD method asks for a response identical to a GET request, but without the response body.
         */
        HEAD,
        /**
         * The DELETE method deletes the specified resource.
         */
        DELETE,
        /**
         * The CONNECT method establishes a tunnel to the server identified by the target resource.
         */
        CONNECT,
        /**
         * The OPTIONS method describes the communication options for the target resource.
         */
        OPTIONS,
        /**
         * The TRACE method performs a message loop-back test along the path to the target resource.
         */
        TRACE,
        /**
         * The PATCH method applies partial modifications to a resource.
         */
        PATCH
    }
}
