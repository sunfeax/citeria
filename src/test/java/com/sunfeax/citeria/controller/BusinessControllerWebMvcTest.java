package com.sunfeax.citeria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
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

    @Test
    void getBusinessesShouldReturnPagedResponse() throws Exception {
        BusinessResponseDto dto = businessDto(1L);
        when(businessService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/businesses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Alpha Studio"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(businessService.getById(1L)).thenReturn(businessDto(1L));

        mockMvc.perform(get("/api/businesses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alpha Studio"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(businessService.getById(99L)).thenThrow(new ResourceNotFoundException("Business with id 99 not found"));

        mockMvc.perform(get("/api/businesses/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Business with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            10L,
            "Alpha Studio",
            "description",
            "+34123456789",
            "business@example.com",
            "https://studio.example.com",
            "Street 1"
        );
        when(businessService.register(any(BusinessPostRequestDto.class))).thenReturn(businessDto(1L));

        mockMvc.perform(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alpha Studio"));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "ownerId": 10,
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
        when(businessService.update(eq(1L), any(BusinessPatchRequestDto.class))).thenReturn(businessDto(1L));

        mockMvc.perform(patch("/api/businesses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "email": "invalid-email"
            }
            """;

        mockMvc.perform(patch("/api/businesses/1")
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
        when(businessService.deactivateById(1L)).thenReturn(businessDto(1L));

        mockMvc.perform(delete("/api/businesses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(businessService.hardDeleteById(1L)).thenReturn(businessDto(1L));

        mockMvc.perform(delete("/api/businesses/1/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(businessService.restoreById(1L)).thenReturn(businessDto(1L));

        mockMvc.perform(patch("/api/businesses/1/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    private BusinessResponseDto businessDto(Long id) {
        return new BusinessResponseDto(
            id,
            "Alpha Studio",
            "description",
            "123456789",
            "business@example.com",
            "https://studio.example.com",
            "Street 1",
            true,
            10L,
            "Owner Name",
            LocalDateTime.of(2026, 1, 1, 12, 0),
            LocalDateTime.of(2026, 1, 2, 12, 0)
        );
    }
}
