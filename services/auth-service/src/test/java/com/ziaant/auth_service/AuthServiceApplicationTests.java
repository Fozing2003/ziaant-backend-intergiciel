package com.ziaant.auth_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziaant.auth_service.model.Role;
import com.ziaant.auth_service.model.StatutCompte;
import com.ziaant.auth_service.model.User;
import com.ziaant.auth_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void protectedProfileEndpointRejectsAnonymousRequests() throws Exception {
		int status = mockMvc.perform(get("/api/users/me"))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(status).isIn(401, 403);
	}

	@Test
	void loginReturnsFiveMinuteAccessTokenAndSevenDayRefreshTokenThenRefreshRotates() throws Exception {
		userRepository.deleteAll();
		userRepository.save(User.builder()
				.name("Client Test")
				.email("client@test.com")
				.phone("699000000")
				.password(passwordEncoder.encode("password123"))
				.role(Role.CLIENT)
				.statut(StatutCompte.APPROUVE)
				.build());

		String loginBody = """
				{"email":"client@test.com","password":"password123"}
				""";

		String loginJson = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginBody))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode login = objectMapper.readTree(loginJson);
		assertThat(login.get("accessToken").asText()).isNotBlank();
		assertThat(login.has("token")).isFalse();
		assertThat(login.get("refreshToken").asText()).isNotBlank();
		assertThat(login.get("accessTokenExpiresIn").asLong()).isEqualTo(300);
		assertThat(login.get("refreshTokenExpiresIn").asLong()).isEqualTo(604800);

		String refreshBody = """
				{"refreshToken":"%s"}
				""".formatted(login.get("refreshToken").asText());

		String refreshJson = mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(refreshBody))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode refresh = objectMapper.readTree(refreshJson);
		assertThat(refresh.get("accessToken").asText()).isNotBlank();
		assertThat(refresh.get("refreshToken").asText()).isNotEqualTo(login.get("refreshToken").asText());
		assertThat(refresh.get("accessTokenExpiresIn").asLong()).isEqualTo(300);
		assertThat(refresh.get("refreshTokenExpiresIn").asLong()).isEqualTo(604800);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(refreshBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void logoutRevokesAccessTokenAndRefreshToken() throws Exception {
		userRepository.deleteAll();
		userRepository.save(User.builder()
				.name("Logout Test")
				.email("logout@test.com")
				.phone("699000001")
				.password(passwordEncoder.encode("password123"))
				.role(Role.CLIENT)
				.statut(StatutCompte.APPROUVE)
				.build());

		String loginJson = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"logout@test.com","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode login = objectMapper.readTree(loginJson);
		String accessToken = login.get("accessToken").asText();
		String refreshToken = login.get("refreshToken").asText();

		mockMvc.perform(get("/api/auth/validate").param("token", accessToken))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString())
						.get("valid").asBoolean()).isTrue());

		mockMvc.perform(post("/api/auth/logout")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refreshToken)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/auth/validate").param("token", accessToken))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString())
						.get("valid").asBoolean()).isFalse());

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refreshToken)))
				.andExpect(status().isBadRequest());
	}
}
