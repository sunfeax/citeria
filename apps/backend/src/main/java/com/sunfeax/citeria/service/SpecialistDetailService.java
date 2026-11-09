package com.sunfeax.citeria.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.dto.specialist.SpecialistDetailResponseDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.OfferingMapper;
import com.sunfeax.citeria.mapper.WorkingHoursMapper;
import com.sunfeax.citeria.repository.OfferingRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.repository.WorkingHoursRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialistDetailService {

    private final UserRepository userRepository;
    private final OfferingRepository offeringRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final OfferingMapper offeringMapper;
    private final WorkingHoursMapper workingHoursMapper;

    @Transactional(readOnly = true)
    public SpecialistDetailResponseDto getDetail(UUID specialistId) {
        UserEntity specialist = userRepository.findById(specialistId)
            .filter(UserEntity::isActive)
            .filter(user -> user.getType() == UserType.SPECIALIST)
            .orElseThrow(() -> new ResourceNotFoundException("Specialist with id " + specialistId + " not found"));

        List<OfferingResponseDto> offerings = offeringRepository.findBySpecialistIdAndIsActiveTrue(specialistId).stream()
            .map(offeringMapper::toResponseDto)
            .toList();

        List<WorkingHoursResponseDto> workingHours =
            workingHoursRepository.findBySpecialistIdAndIsActiveTrue(specialistId).stream()
                .map(workingHoursMapper::toResponseDto)
                .toList();

        return new SpecialistDetailResponseDto(
            specialist.getId(),
            specialist.getFirstName(),
            specialist.getLastName(),
            specialist.isActive(),
            offerings,
            workingHours
        );
    }
}
