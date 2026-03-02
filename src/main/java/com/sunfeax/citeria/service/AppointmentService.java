package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAll() {
        return appointmentRepository.findAll()
            .stream()
            .map(appointmentMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AppointmentResponseDto> getById(Long id) {
        return appointmentRepository.findById(id)
            .map(appointmentMapper::toResponseDto);
    }
}
