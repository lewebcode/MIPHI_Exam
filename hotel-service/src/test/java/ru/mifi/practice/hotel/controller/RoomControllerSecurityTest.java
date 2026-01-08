package ru.mifi.practice.hotel.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import ru.mifi.practice.hotel.security.SecurityConfig;
import ru.mifi.practice.hotel.service.RoomCrudService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoomController.class)
@Import(SecurityConfig.class)
class RoomControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RoomCrudService roomCrudService;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessStats() throws Exception {
        Mockito.when(roomCrudService.stats(null)).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/rooms/stats")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }
}
