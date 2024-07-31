package org.example;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Request {
    private MultiMap queryParams = new MultiValueMap();
    private String queryParamsPath;
    private String method;
    private String version;
    private List<NameValuePair> headers;

    public Request(String url, String method, String version, List<NameValuePair> headers) {
        this.method = method;
        this.version = version;
        this.headers = headers;
        parseUrl(url);
    }

    private void parseUrl(String url) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName() != null && param.getValue() != null)
                    queryParams.put(param.getName(), param.getValue());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        int i = url.indexOf("?");
        this.queryParamsPath = (i == -1) ? url : url.substring(0, i);
    }

    public MultiMap getQueryParams() {
        return queryParams;
    }

    public List<String> getQueryParam(String key) {
        return (List<String>) queryParams.get(key);
    }

    public String getQueryParamsPath() {
        return queryParamsPath;
    }

    public String getMethod() {
        return method;
    }

    public String getVersion() {
        return version;
    }

    public List<NameValuePair> getHeaders() {
        return headers;
    }
}

