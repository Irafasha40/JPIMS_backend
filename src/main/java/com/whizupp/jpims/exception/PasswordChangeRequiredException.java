package com.whizupp.jpims.exception;

/** Carries a short-lived JWT {@code tempToken} for {@code POST /api/auth/complete-first-login}. */
public class PasswordChangeRequiredException extends RuntimeException {
    public PasswordChangeRequiredException(String tempToken) {
        super(tempToken);
    }
}
