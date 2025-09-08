package com.cleveronion.blog.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错误响应类
 * 用于封装详细的错误信息
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Schema(description = "错误响应")
public class ErrorResponse {
    
    /**
     * 错误码
     */
    @Schema(description = "错误码", example = "VALIDATION_ERROR")
    private String errorCode;
    
    /**
     * 错误消息
     */
    @Schema(description = "错误消息", example = "参数验证失败")
    private String message;
    
    /**
     * 详细错误信息
     */
    @Schema(description = "详细错误信息")
    private String details;
    
    /**
     * 请求路径
     */
    @Schema(description = "请求路径", example = "/api/articles")
    private String path;
    
    /**
     * 时间戳
     */
    @Schema(description = "错误发生时间")
    private LocalDateTime timestamp;
    
    /**
     * 字段验证错误列表
     */
    @Schema(description = "字段验证错误列表")
    private List<FieldError> fieldErrors;
    
    /**
     * 默认构造函数
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public ErrorResponse(String errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误消息
     * @param path 请求路径
     */
    public ErrorResponse(String errorCode, String message, String path) {
        this(errorCode, message);
        this.path = path;
    }
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误消息
     * @param details 详细信息
     * @param path 请求路径
     */
    public ErrorResponse(String errorCode, String message, String details, String path) {
        this(errorCode, message, path);
        this.details = details;
    }
    
    // Getters and Setters
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    /**
     * 字段错误类
     */
    @Schema(description = "字段验证错误")
    public static class FieldError {
        
        /**
         * 字段名
         */
        @Schema(description = "字段名", example = "title")
        private String field;
        
        /**
         * 拒绝的值
         */
        @Schema(description = "拒绝的值", example = "")
        private Object rejectedValue;
        
        /**
         * 错误消息
         */
        @Schema(description = "错误消息", example = "标题不能为空")
        private String message;
        
        /**
         * 构造函数
         * 
         * @param field 字段名
         * @param rejectedValue 拒绝的值
         * @param message 错误消息
         */
        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }
        
        // Getters and Setters
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}