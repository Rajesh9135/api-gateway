package com.api.filter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class RouteValidator {

	@Value("${open.api.endpoints}")
    private String openApiEndpoints;

    private List<String> openApiEndpointsList;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        if (openApiEndpoints != null) {
            openApiEndpointsList = Arrays.asList(openApiEndpoints.split(","));
        }
    }

    public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpointsList.stream()
            .noneMatch(uri -> pathMatcher.match(uri, request.getURI().getPath()));
}