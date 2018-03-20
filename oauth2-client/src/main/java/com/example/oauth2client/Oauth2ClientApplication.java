package com.example.oauth2client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Oauth2ClientApplication {

	@Bean
	RestOperations restTemplate(OAuth2AuthorizedClientService cs) {
		return new RestTemplateBuilder()
				.interceptors((ClientHttpRequestInterceptor) (httpRequest, bytes, clientHttpRequestExecution) -> {
					OAuth2AuthenticationToken token = OAuth2AuthenticationToken.class.cast(
							SecurityContextHolder.getContext().getAuthentication());
					OAuth2AuthorizedClient client = cs.loadAuthorizedClient(
							token.getAuthorizedClientRegistrationId(),
							token.getName());
					httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
					return clientHttpRequestExecution.execute(httpRequest, bytes);
				})
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(Oauth2ClientApplication.class, args);
	}
}

@RestController
class ProfileRestController {

	private final OAuth2AuthorizedClientService cs;
	private final RestOperations restTemplate;

	ProfileRestController(OAuth2AuthorizedClientService cs, RestOperations restTemplate) {
		this.cs = cs;
		this.restTemplate = restTemplate;
	}

	@GetMapping("/")
	PrincipalDetails profile(OAuth2AuthenticationToken token) {

		OAuth2AuthorizedClient client = cs.loadAuthorizedClient(
				token.getAuthorizedClientRegistrationId(),
				token.getName());

		String userInfoUri = client.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUri();

		return restTemplate.exchange(
				userInfoUri, HttpMethod.GET, null, PrincipalDetails.class)
				.getBody();
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PrincipalDetails {
		private String name;
	}
}