package it.labforweb.streamlabapi.controllers;

import it.labforweb.streamlabapi.dtos.TmdbResponse;
import it.labforweb.streamlabapi.services.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbProxyController {

    private final TmdbService tmdbService;

    public TmdbProxyController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/{*path}")
    public ResponseEntity<TmdbResponse> proxyTmdb(@PathVariable String path,
                                                  @RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(tmdbService.forwardRequest(path, params));
    }
}