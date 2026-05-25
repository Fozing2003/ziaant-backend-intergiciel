package com.ziaant.restaurant_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void publicRestaurantListIsAccessibleWithoutToken() throws Exception {
		int status = mockMvc.perform(get("/api/restaurants"))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(status).isEqualTo(200);
	}

	@Test
	void privateRestaurantEndpointRejectsAnonymousRequests() throws Exception {
		int status = mockMvc.perform(get("/api/restaurants/mes-restaurants"))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(status).isIn(401, 403);
	}
}
