package com.example.oauth2client;

import lombok.Data;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

@SpringBootApplication
public class Oauth2ClientApplication {

	@Bean
	@RequestScope
	OAuth2AuthenticationToken token() {
		return OAuth2AuthenticationToken.class.cast(
				SecurityContextHolder.getContext().getAuthentication());
	}

	@Bean
	@RequestScope
	OAuth2AuthorizedClient client(OAuth2AuthorizedClientService clientService,
	                              OAuth2AuthenticationToken token) {
		return clientService.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(),
				token.getName());
	}

	@Bean
	@RequestScope
	RestTemplate restTemplate(OAuth2AuthorizedClient client) {
		return new RestTemplateBuilder()
				.interceptors((ClientHttpRequestInterceptor) (httpRequest, bytes, clientHttpRequestExecution) -> {
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
	private final RestTemplate restTemplate;
	private final OAuth2AuthorizedClient client;

	ProfileRestController(RestTemplate restTemplate,
	                      OAuth2AuthorizedClient authorizedClient) {
		this.restTemplate = restTemplate;
		this.client = authorizedClient;
	}

	@GetMapping("/profile")
	PrincipalDetails profile(/*OAuth2AuthenticationToken token*/) {

		/*OAuth2AuthorizedClient auth2AuthorizedClient = clientService.loadAuthorizedClient(
				token.getAuthorizedClientRegistrationId(),
				token.getName()
		)*/
		;
//		String accessToken = auth2AuthorizedClient.getAccessToken().getTokenValue();
		String userInfoUri = client.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUri();
/*

		URI url = URI.create(userInfoUri);
		RequestEntity<Void> requestEntity =
				RequestEntity
						.get(url)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.build();
*/


		return restTemplate.exchange(
				userInfoUri, HttpMethod.GET, null, PrincipalDetails.class)
				.getBody();
	}

	@Data
	public static class PrincipalDetails {
		private String name;
	}
}