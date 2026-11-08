package com.sunfeax.citeria.security;

public record GoogleProfile(
    String subject,
    String email,
    boolean emailVerified,
    String givenName,
    String familyName
) {}
