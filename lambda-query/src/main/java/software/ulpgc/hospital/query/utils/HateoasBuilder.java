package software.ulpgc.hospital.query.utils;

public class HateoasBuilder {
    public static <T> HateoasResponse<T> forCollection(T data, String selfPath) {
        HateoasResponse<T> response = new HateoasResponse<>(data)
                .addLink("self", selfPath, "GET");
        switch (selfPath) {
            case "/events":
                response.addTemplatedLink("search",
                        "/events{?eventType,date,department,admissionType,bedNumber}",
                        "GET");
                break;
            case "/stats":
                response.addTemplatedLink("search",
                        "/stats{?department,date,minAdmissions}",
                        "GET");
                break;
        }
        return response.addLink("events", "/events", "GET")
                .addLink("stats", "/stats", "GET");
    }

    public static <T> HateoasResponse<T> forSingleResource(T data, String selfPath, String collectionPath) {
        return new HateoasResponse<>(data)
                .addLink("self", selfPath, "GET")
                .addLink("collection", collectionPath, "GET")
                .addLink("events", "/events", "GET")
                .addLink("stats", "/stats", "GET");
    }
}
