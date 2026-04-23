package com.invex.employees.mapper;

import com.invex.employees.constants.EmployeeConstants;
import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .secondName(dto.getSecondName())
                .paternalSurname(dto.getPaternalSurname())
                .maternalSurname(dto.getMaternalSurname())
                .age(dto.getAge())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .position(dto.getPosition())
                .active(Optional.ofNullable(dto.getActive()).orElse(EmployeeConstants.DEFAULT_ACTIVE_STATUS))
                .build();
    }

    public EmployeeResponseDto toResponseDto(Employee entity) {
        return EmployeeResponseDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .secondName(entity.getSecondName())
                .paternalSurname(entity.getPaternalSurname())
                .maternalSurname(entity.getMaternalSurname())
                .age(entity.getAge())
                .gender(entity.getGender())
                .birthDate(entity.getBirthDate())
                .position(entity.getPosition())
                .createdAt(entity.getCreatedAt())
                .active(entity.getActive())
                .build();
    }

    public void updateEntityFromDto(EmployeeRequestDto dto, Employee entity) {
        Optional.ofNullable(dto.getFirstName()).ifPresent(entity::setFirstName);
        Optional.ofNullable(dto.getSecondName()).ifPresent(entity::setSecondName);
        Optional.ofNullable(dto.getPaternalSurname()).ifPresent(entity::setPaternalSurname);
        Optional.ofNullable(dto.getMaternalSurname()).ifPresent(entity::setMaternalSurname);
        Optional.ofNullable(dto.getAge()).ifPresent(entity::setAge);
        Optional.ofNullable(dto.getGender()).ifPresent(entity::setGender);
        Optional.ofNullable(dto.getBirthDate()).ifPresent(entity::setBirthDate);
        Optional.ofNullable(dto.getPosition()).ifPresent(entity::setPosition);
        Optional.ofNullable(dto.getActive()).ifPresent(entity::setActive);
    }
}
