package com.sunfeax.citeria.normalizer;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;

@Component
public class SpecialistServiceFieldNormalizer {

    public SpecialistServicePostRequestDto normalizePostRequest(SpecialistServicePostRequestDto request) {
        return request;
    }

    public SpecialistServicePatchRequestDto normalizePatchRequest(SpecialistServicePatchRequestDto request) {
        return request;
    }
}
