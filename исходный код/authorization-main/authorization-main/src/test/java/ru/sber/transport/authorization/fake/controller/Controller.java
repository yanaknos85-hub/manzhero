package ru.sber.transport.authorization.fake.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;

import java.util.UUID;

@RestController
public class Controller implements IController {

    public String authorizeToken(Authentication authentication) {
        return ((Jwt) authentication.getPrincipal()).getId();
    }

    public String authorizeSudir(Authentication authentication) {
        return authentication.getName();
    }

    public String noAuthorize(@RequestParam("data") String data) {
        return data;
    }

    public String authorizeRequired(String data) {
        return data;
    }

    public String authorizeRequiredRolePost(@RequestParam("data") String data) {
        return data;
    }

    public String authorizeRequiredRolePost2(@RequestParam("data") String data) {
        return data;
    }

    public String authorizeRequiredRoleGet(@RequestParam("data") String data) {
        return data;
    }

    public String transportTypeGet(@RequestParam("data") String data) {
        return data;
    }

    public String transportTypeGet(@PathVariable("organizationId") UUID id) {
        return id.toString();
    }
    
    @CheckOrganizationAccess
    public String organizationalDatAccess(@Organization @PathVariable("organizationId") UUID id) {
        return id.toString();
    }

    @CheckOrganizationAccess
    public String organizationalDataAccess() {
        return "ok";
    }
    
    @CheckOrganizationAccess
    public String organizationalDatAccessNoOrganization() {
        return "success";
    }
    
    @CheckOrganizationAccess
    public String organizationalDatAccessField(@Organization("organizationId") @RequestBody TestDto dto) {
        return dto.getOrganizationId().toString();
    }

    @Override
    public String checkConsentAccess() {
        return "ok";
    }

    @Override
    public String checkSkipConsentAccess() {
        return "ok";
    }

    @Override
    public String skipUrlInConfig() {
        return "ok";
    }
    
}
