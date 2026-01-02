package software.ulpgc.hospital.query.app.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import software.ulpgc.hospital.query.domain.control.Command;
import software.ulpgc.hospital.query.domain.control.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandFactory {
    private final Map<String, Builder> builderMap;

    public CommandFactory() {
        builderMap = new HashMap<>();
    }

    public static CommandFactory create() {
        return new CommandFactory();
    }

    public Set<String> keySet() {
        return builderMap.keySet();
    }

    public CommandFactory register(String name, Builder builder) {
        builderMap.put(name, builder);
        return this;
    }

    public Selector with(APIGatewayProxyRequestEvent request, Response response) {
        return name -> builderMap.get(name).build(request, response);
    }

    public interface Builder {
        Command build(APIGatewayProxyRequestEvent request, Response response);
    }

    public interface Selector {
        Command build(String name);
    }
}