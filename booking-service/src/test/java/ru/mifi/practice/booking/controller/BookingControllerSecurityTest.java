package ru.mifi.practice.booking.controller;

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
import ru.mifi.practice.booking.security.SecurityConfig;
import ru.mifi.practice.booking.service.BookingWorkflowService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookingWorkflowService bookingWorkflowService;

    @MockBean
    JwtDecoder jwtDecoder; // stub for security

    @Test
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/booking/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbiddenForUserAccessingOthers() throws Exception {
        Mockito.when(bookingWorkflowService.getById(Mockito.eq(1L), Mockito.anyString(), Mockito.eq(false)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(get("/booking/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject("user"))))
                .andExpect(status().isForbidden());
    }
}
