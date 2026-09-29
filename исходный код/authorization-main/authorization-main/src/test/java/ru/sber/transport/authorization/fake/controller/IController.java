package ru.sber.transport.authorization.fake.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;

import java.util.UUID;

@RequestMapping
public interface IController {

    @PostMapping("/login/token/")
    String authorizeToken(Authentication authentication);

    @PatchMapping("/login/sudir/")
    String authorizeSudir(Authentication authentication);

    @NoAuthorize
    @PostMapping("/noAuthorize/")
    String noAuthorize(@RequestParam("data") String data);

    @PutMapping("/authorize/")
    String authorizeRequired(@RequestParam(value = "data", required = false) String data);

    @DeleteMapping("/authorize/role/")
    String authorizeRequiredRolePost(@RequestParam("data") String data);

    @PostMapping("/authorize/role/2/")
    String authorizeRequiredRolePost2(@RequestParam("data") String data);

    @GetMapping("/authorize/role/")
    String authorizeRequiredRoleGet(@RequestParam("data") String data);

    @GetMapping("/transport-types/")
    String transportTypeGet(@RequestParam("data") String data);

    @GetMapping("/{organizationId}/")
    String transportTypeGet(@PathVariable("organizationId") UUID id);

    @CheckOrganizationAccess
    @GetMapping("/access/path/{organizationId}/")
    String organizationalDatAccess(@Organization @PathVariable("organizationId") UUID id);

    @CheckOrganizationAccess
    @GetMapping("/access/path/")
    String organizationalDataAccess();

    @CheckOrganizationAccess
    @GetMapping("/access/path/no_organization/")
    String organizationalDatAccessNoOrganization();

    @CheckOrganizationAccess
    @PostMapping(value = "/access/field/", consumes = MediaType.APPLICATION_JSON_VALUE)
    String organizationalDatAccessField(@Organization("organizationId") @RequestBody TestDto dto);

    @GetMapping("/consent/path/")
    String checkConsentAccess();

    @SkipConsentCheck
    @GetMapping("/consent-skip/path/")
    String checkSkipConsentAccess();

    @NoAuthorize
    @GetMapping("/skipUrlInConfig/")
    String skipUrlInConfig();
}
