package software.ulpgc.hospital.query.utils;

import java.util.HashMap;
import java.util.Map;

public class HateoasResponse<T> {
    private final T data;
    private final Map<String, Link> links;

    public HateoasResponse(T data) {
        this.data = data;
        this.links = new HashMap<>();
    }

    public HateoasResponse<T> addLink(String rel, String href, String method) {
        this.links.put(rel, new Link(href, rel, method));
        return this;
    }

    public void addTemplatedLink(String rel, String href, String method) {
        this.links.put(rel, new Link(href, rel, method, true));
    }

    public T getData() { return data; }
    public Map<String, Link> getLinks() { return links; }

    public static class Link {
        private final String href;
        private final String rel;
        private final String method;
        private final boolean templated;

        public Link(String href, String rel, String method) {
            this(href, rel, method, false);
        }

        public Link(String href, String rel, String method, boolean templated) {
            this.href = href;
            this.rel = rel;
            this.method = method;
            this.templated = templated;
        }

        public String getHref() { return href; }
        public String getRel() { return rel; }
        public String getMethod() { return method; }
        public boolean isTemplated() { return templated; }
    }
}
