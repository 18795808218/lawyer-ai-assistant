package com.quince.lawyeraiassistant.common.response;

public record ApiResponse<T>(
        String code,
        String message,
        T data) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "success";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                SUCCESS_CODE,
                SUCCESS_MESSAGE,
                data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(
                SUCCESS_CODE,
                SUCCESS_MESSAGE,
                null);
    }
}