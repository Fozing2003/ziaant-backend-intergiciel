package com.ziaant.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ziaant.notification_service.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

	@MockitoBean
	private NotificationService notificationService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void emailEndpointRejectsMissingInternalToken() throws Exception {
		int status = mockMvc.perform(post("/api/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"to":"user@gmail.com","subject":"Hello","body":"Body"}
								"""))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(status).isIn(401, 403);
	}

	@Test
	void emailEndpointAcceptsValidInternalToken() throws Exception {
		int status = mockMvc.perform(post("/api/notifications/email")
						.header("X-Internal-Token", "test-internal-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"to":"user@gmail.com","subject":"Hello","body":"Body"}
								"""))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(status).isEqualTo(200);
	}
}
