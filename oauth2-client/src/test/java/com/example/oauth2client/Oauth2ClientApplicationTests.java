package com.example.oauth2client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestOperations;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest // why doesnt @WebMvcTest work?
public class Oauth2ClientApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RestOperations restTemplate;

	@Test
	public void oauth() throws Exception {
		Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("USER"));
		OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", "rob"), "name");
		OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oAuth2User, authorities, "client-login");

		Mockito.when(this.restTemplate.exchange(
				Mockito.any(String.class),
				Mockito.any(HttpMethod.class),
				Mockito.any(HttpEntity.class),
				Mockito.any(ProfileRestController.PrincipalDetails.class.getClass())))
				.thenReturn(ResponseEntity.ok(new ProfileRestController.PrincipalDetails("rob")));

		this.mockMvc.perform(get("/").with(authentication(token)))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.jsonPath("@.name").value("rob"));
	}

}
