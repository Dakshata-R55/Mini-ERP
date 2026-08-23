package com.Mini_ERP.security;

import java.time.Instant;

public record TokenDetails(String token, String jti, Instant expiresAt) {}