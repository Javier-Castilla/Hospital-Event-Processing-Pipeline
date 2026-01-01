package software.ulpgc.hospital.query.domain.utils;

import java.util.Map;


public class HateoasResponse<T> {
    private final T data;
    private final Map<String, Link> links;

    public HateoasResponse(T data, Map<String, Link> links) {
        this.data = data;
        this.links = links;
    }

    public HateoasResponse<T> addLink(String relation, String href, String method) {
        this.links.put(relation, new Link(href, relation, method));
        return this;
    }
}
