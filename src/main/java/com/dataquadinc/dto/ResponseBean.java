
package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "success", "message", "data", "error" })
public class ResponseBean {
    private boolean success;
    private String message;
    private Object data;
    private String error;

    public ResponseBean(boolean success, String message, String error, Object data) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    // Helper methods for easy instantiation of successful and error responses
    public static ResponseBean successResponse(String message, Object data) {
        return new ResponseBean(true, message, null, data);
    }

    public static ResponseBean errorResponse(String message, String error) {
        return new ResponseBean(false, message, error, null);
    }
}
