package ru.sber.transport.authorization.fake.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Jacksonized
@Builder
public class TestDto {
    
    private final UUID organizationId;
    
}
