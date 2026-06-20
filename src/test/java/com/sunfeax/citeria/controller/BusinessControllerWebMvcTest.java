package com.sunfeax.citeria.controller;

import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.config.JwtAuthenticationFilter;
import com.sunfeax.citeria.service.BusinessService;

@WebMvcTest(BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BusinessControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BusinessService businessService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID ID = new UUID(0, 1L);
    private static final UUID MISSING_ID = new UUID(0, 99L);

    @Test
    void getBusinessesShouldReturnPagedResponse() throws Exception {
        BusinessResponseDto dto = businessDto(new UUID(0, 1L));
        when(businessService.list(any(), any(), any())).thenReturn(new PageResponseDto<>(List.of(dto), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/businesses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Alpha Studio"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(businessService.getById(new UUID(0, 1L))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(get("/api/businesses/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Alpha Studio"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(businessService.getById(new UUID(0, 99L))).thenThrow(new ResourceNotFoundException("Business with id 99 not found"));

        mockMvc.perform(get("/api/businesses/" + MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Business with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            "Alpha Studio",
            "description",
            "+34123456789",
            "business@example.com",
            "https://studio.example.com",
            "Street 1"
        );
        when(businessService.create(any(BusinessPostRequestDto.class))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Alpha Studio"));
    }

    @Test
    void createShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "name": "",
              "description": "description",
              "phone": "+34123456789",
              "email": "business@example.com",
              "website": "https://studio.example.com",
              "address": "Street 1"
            }
            """;

        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(
            null,
            "Alpha Studio Updated",
            null,
            null,
            null,
            null,
            null
        );
        when(businessService.update(eq(new UUID(0, 1L)), any(BusinessPatchRequestDto.class))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(patch("/api/businesses/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void updateShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "email": "invalid-email"
            }
            """;

        mockMvc.perform(patch("/api/businesses/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(businessService.deactivateById(new UUID(0, 1L))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(delete("/api/businesses/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(businessService.hardDeleteById(new UUID(0, 1L))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(delete("/api/businesses/" + ID + "/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(businessService.restoreById(new UUID(0, 1L))).thenReturn(businessDto(new UUID(0, 1L)));

        mockMvc.perform(patch("/api/businesses/" + ID + "/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    private BusinessResponseDto businessDto(UUID id) {
        return new BusinessResponseDto(
            id,
            "Alpha Studio",
            "description",
            "123456789",
            "business@example.com",
            "https://studio.example.com",
            "Street 1",
            true,
            new UUID(0, 10L),
            "Owner Name",
            Instant.parse("2026-01-01T12:00:00Z"),
            Instant.parse("2026-01-02T12:00:00Z")
        );
    }
}
