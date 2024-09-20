package com.api.filter;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import com.api.response.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

	@Autowired
	private RouteValidator routeValidator;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${token.url}")
	private String tokenUrl;

	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			if (request.getMethod() == HttpMethod.OPTIONS) {
				exchange.getResponse().setStatusCode(HttpStatus.OK);
				return exchange.getResponse().setComplete();
			}
			if (routeValidator.isSecured.test(request)) {
				HttpHeaders headers = request.getHeaders();
				if (!headers.containsKey(HttpHeaders.AUTHORIZATION) || !headers.containsKey("userId")
						|| !headers.containsKey("Source")) {
					return unauthorizedResponse(exchange, "Missing Authorization header or userId or Source");
				}
				String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
				String userId = headers.getFirst("userId");
				String web = headers.getFirst("source");
				try {
					HttpHeaders requestHeaders = new HttpHeaders();
					requestHeaders.set("Authorization", authHeader);
					requestHeaders.set("userId", userId);
					requestHeaders.set("source", web);
					requestHeaders.set("Content-Type", "application/json");
					logger.info("[Token Url ] [ " + tokenUrl + " ]");
					URI uri = new URI(tokenUrl);
					HttpEntity<TokenResponse> requestEntity = new HttpEntity<>(null, requestHeaders);
					ResponseEntity<TokenResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET,requestEntity, TokenResponse.class);
					TokenResponse responseObj = responseEntity.getBody();
					if (responseObj.getStatusDescription().getStatusCode() != 200) {
						return unauthorizedResponse(exchange, responseObj);
					}
				} catch (Exception e) {
					logger.info("{Exception getting on authentication :: }" + e);
					return unauthorizedResponse(exchange, "Unauthorized user trying to access the application");
				}
			}
			return chain.filter(exchange);
		};
	}

	private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, Object body) {
		try {
			exchange.getResponse().setStatusCode(HttpStatus.OK);
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonResponse = objectMapper.writeValueAsString(body);
			return exchange.getResponse()
					.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes())));
		} catch (Exception e) {
			return Mono.error(e);
		}
	}

	public static class Config {
		// Configuration properties, if needed
	}
}
