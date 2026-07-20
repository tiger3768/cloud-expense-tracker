package com.aditya.expensetracker.expense_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    User toEntity(RegisterRequest request);

}