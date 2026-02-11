package com.teamhub.utils;

import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.regex.Pattern;

public final class ValidationHelper {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern OBJECT_ID_PATTERN =
            Pattern.compile("^[a-fA-F0-9]{24}$");

    private ValidationHelper() {
        // Utility class
    }

    /**
     * Validate that a required field exists in the JSON body.
     */
    public static void requireField(JsonObject body, String field) {
        if (body == null || !body.containsKey(field) || body.getValue(field) == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Field '" + field + "' is required");
        }
    }

    /**
     * Validate that all required fields exist.
     */
    public static void requireFields(JsonObject body, List<String> fields) {
        for (String field : fields) {
            requireField(body, field);
        }
    }

    /**
     * Validate that a string field is not blank.
     */
    public static void requireNonBlank(JsonObject body, String field) {
        requireField(body, field);
        String value = body.getString(field);
        if (value == null || value.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Field '" + field + "' must not be blank");
        }
    }

    /**
     * Validate email format.
     */
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid email format");
        }
    }

    /**
     * Validate string length.
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must not be null");
        }
        if (value.length() < minLength || value.length() > maxLength) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must be between " + minLength + " and " + maxLength + " characters");
        }
    }

    /**
     * Validate MongoDB ObjectId format.
     */
    public static void validateObjectId(String id) {
        if (id == null || !OBJECT_ID_PATTERN.matcher(id).matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid ID format");
        }
    }

    /**
     * Validate that a value is one of the allowed values.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void validateEnum(String value, String fieldName, Class<? extends Enum> enumClass) {
        if (value == null) {
            return;
        }
        try {
            Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid value for '" + fieldName + "': " + value);
        }
    }
}
