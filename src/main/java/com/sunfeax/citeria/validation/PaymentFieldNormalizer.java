package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;

@Component
public class PaymentFieldNormalizer {

    public PaymentPostRequestDto normalizePostRequest(PaymentPostRequestDto request) {
        return request;
    }

    public PaymentPatchRequestDto normalizePatchRequest(PaymentPatchRequestDto request) {
        return request;
    }
}
