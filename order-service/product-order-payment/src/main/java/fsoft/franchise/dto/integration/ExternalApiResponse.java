package fsoft.franchise.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Generic wrapper matching the common API response format:
 * { "code": 1000, "message": "Success", "result": T }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalApiResponse<T>(
        int code,
        String message,
        T result
) {}
