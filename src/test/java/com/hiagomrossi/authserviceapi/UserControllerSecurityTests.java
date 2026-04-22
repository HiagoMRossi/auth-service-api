package com.hiagomrossi.authserviceapi;

import com.hiagomrossi.authserviceapi.config.JwtAuthenticationFilter;
import com.hiagomrossi.authserviceapi.config.SecurityConfig;
import com.hiagomrossi.authserviceapi.controller.UserController;
import com.hiagomrossi.authserviceapi.service.CustomUserDetailsService;
import com.hiagomrossi.authserviceapi.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void meWithoutTokenShouldReturnUnauthorizedOrForbidden() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403);
                });
    }

    @Test
    void meWithValidTokenShouldReturnOk() throws Exception {
        String token = "valid-token";
        String email = "hiago@example.com";

        UserDetails userDetails = new User(email, "encoded-password", java.util.Collections.emptyList());

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You are authenticated"));
    }
}
