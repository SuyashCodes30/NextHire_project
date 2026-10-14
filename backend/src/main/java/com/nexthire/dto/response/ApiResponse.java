package com.nexthire.dto.response;

/**
 * ApiResponse — Standard wrapper for ALL API responses.
 *
 * Every endpoint returns this structure so the frontend
 * always gets a consistent format.
 *
 * Success response example:
 * {
 *   "success": true,
 *   "message": "Interview created successfully",
 *   "data": { ... actual data ... },
 *   "status": 201
 * }
 *
 * Error response example:
 * {
 *   "success": false,
 *   "message": "Interview not found",
 *   "data": null,
 *   "status": 404
 * }
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;       // Generic — can be any object, list, or null
    private int status;   // HTTP status code

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
    }

    // ---------------------------------------------------------------
    // Static Factory Methods — for convenience
    // ---------------------------------------------------------------

    /**
     * Create a success response with data.
     */
    public static <T> ApiResponse<T> success(String message, T data, int status) {
        return new ApiResponse<>(true, message, data, status);
    }

    /**
     * Create a success response with 200 OK.
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String message, int status) {
        return new ApiResponse<>(false, message, null, status);
    }

    // ---------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
