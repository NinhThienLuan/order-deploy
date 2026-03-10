package fsoft.franchise.service;

import fsoft.franchise.dto.auth.LoginRequest;
import fsoft.franchise.dto.auth.AccountResponse;
import fsoft.franchise.dto.auth.LoginResponse;

public interface AccountsService {

    /** Authenticate and return JWT tokens in a LoginResponse. */
    LoginResponse login(LoginRequest request);

    /** Return the full profile of the account identified by email. */
    AccountResponse getMe(String email);

    /**
     * Invalidate the current session by clearing server-side state.
     * Since there is no Redis, logout is handled by the controller clearing the
     * cookie.
     * This method exists as a hook for future server-side invalidation (e.g. token
     * blacklist).
     */
    void logout();
}
