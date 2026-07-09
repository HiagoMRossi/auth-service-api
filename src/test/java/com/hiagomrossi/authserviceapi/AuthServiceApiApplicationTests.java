package com.hiagomrossi.authserviceapi;

import com.hiagomrossi.authserviceapi.config.JwtAuthenticationFilter;
import com.hiagomrossi.authserviceapi.controller.AuthController;
import com.hiagomrossi.authserviceapi.dto.LoginResponse;
import com.hiagomrossi.authserviceapi.dto.RegisterResponse;
import com.hiagomrossi.authserviceapi.entity.Role;
import com.hiagomrossi.authserviceapi.exception.EmailAlreadyExistsException;
import com.hiagomrossi.authserviceapi.exception.GlobalExceptionHandler;
import com.hiagomrossi.authserviceapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthServiceApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	void registerShouldReturnCreated() throws Exception {
		RegisterResponse response = new RegisterResponse();
		response.setId(1L);
		response.setName("Hiago Rossi");
		response.setEmail("hiago@example.com");
		response.setRole(Role.USER.name());
		response.setCreatedAt(LocalDateTime.now());

		when(authService.register(any())).thenReturn(response);

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Hiago Rossi",
								  "email": "hiago@example.com",
								  "password": "123456"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Hiago Rossi"))
				.andExpect(jsonPath("$.email").value("hiago@example.com"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void registerWithDuplicateEmailShouldReturnConflict() throws Exception {
		when(authService.register(any())).thenThrow(new EmailAlreadyExistsException());

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Hiago Rossi",
								  "email": "hiago@example.com",
								  "password": "123456"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Conflict"))
				.andExpect(jsonPath("$.message").value("Email already exists"));
	}

	@Test
	void loginShouldReturnOkWithToken() throws Exception {
		LoginResponse response = new LoginResponse();
		response.setAccessToken("fake-access-token");
		response.setRefreshToken("fake-refresh-token");
		response.setType("Bearer");

		when(authService.login(any())).thenReturn(response);

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "hiago@example.com",
								  "password": "123456"
								}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("fake-access-token"))
				.andExpect(jsonPath("$.refreshToken").value("fake-refresh-token"))
				.andExpect(jsonPath("$.type").value("Bearer"));
	}

	@Test
	void refreshShouldReturnNewTokens() throws Exception {
		LoginResponse response = new LoginResponse();
		response.setAccessToken("new-access-token");
		response.setRefreshToken("new-refresh-token");
		response.setType("Bearer");

		when(authService.refresh(any())).thenReturn(response);

		mockMvc.perform(post("/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "valid-refresh-token"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("new-access-token"))
				.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
				.andExpect(jsonPath("$.type").value("Bearer"));
	}

	@Test
	void logoutShouldReturnNoContent() throws Exception {
		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "refresh-token"
								}
								"""))
				.andExpect(status().isNoContent());

		verify(authService).logout(eq("access-token"), any());
	}

}
