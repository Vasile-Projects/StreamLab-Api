package it.labforweb.streamlabapi.services;

import it.labforweb.streamlabapi.dtos.TmdbResponse;
import it.labforweb.streamlabapi.security.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class TmdbService {

    private final TmdbProperties tmdbProperties;
    private final RestClient restClient;

    public TmdbService(TmdbProperties tmdbProperties) {
        this.tmdbProperties = tmdbProperties;
        this.restClient = RestClient.builder()
                .baseUrl(tmdbProperties.getBaseUrl())
                .build();
    }

    public TmdbResponse forwardRequest(String path, MultiValueMap<String, String> params) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/" + path);
                    params.forEach((key, values) ->
                            values.forEach(v -> uriBuilder.queryParam(key, v)));
                    uriBuilder.queryParam("api_key", tmdbProperties.getApiKey());
                    return uriBuilder.build();
                })
                .retrieve()
                .body(TmdbResponse.class);
    }
}