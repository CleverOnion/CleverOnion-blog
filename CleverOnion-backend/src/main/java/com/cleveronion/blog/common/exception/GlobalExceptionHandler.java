package com.cleveronion.blog.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.cleveronion.blog.presentation.api.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，并返回标准化的错误响应
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理Sa-Token未登录异常
     * 
     * @param ex 未登录异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<ErrorResponse>> handleNotLoginException(
            NotLoginException ex, HttpServletRequest request) {
        
        logger.warn("用户未登录: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_LOGIN",
            "用户未登录，请先登录",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Result.unauthorized("用户未登录，请先登录"));
    }

    /**
     * 处理业务异常
     * 
     * @param ex 业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<ErrorResponse>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logger.warn("业务异常: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.error(400, ex.getMessage()));
    }
    
    /**
     * 处理资源未找到异常
     * 
     * @param ex 资源未找到异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<ErrorResponse>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("资源未找到: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.notFound(ex.getMessage()));
    }
    
    /**
     * 处理权限不足异常
     * 
     * @param ex 权限不足异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("权限不足: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Result.forbidden(ex.getMessage()));
    }
    
    /**
     * 处理重复资源异常
     * 
     * @param ex 重复资源异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Result<ErrorResponse>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        
        logger.warn("资源重复: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Result.error(409, ex.getMessage()));
    }
    
    /**
     * 处理参数验证异常
     * 
     * @param ex 参数验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Result<ErrorResponse>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("参数验证失败: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(ex.getMessage()));
    }
    
    /**
     * 处理方法参数验证异常
     * 
     * @param ex 方法参数验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("方法参数验证失败: {}", ex.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "参数验证失败",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        String message = fieldErrors.isEmpty() ? "参数验证失败" : 
            fieldErrors.get(0).getField() + ": " + fieldErrors.get(0).getMessage();
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(message));
    }
    
    /**
     * 处理绑定异常
     * 
     * @param ex 绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<ErrorResponse>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        logger.warn("参数绑定失败: {}", ex.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "BIND_ERROR",
            "参数绑定失败",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        String message = fieldErrors.isEmpty() ? "参数绑定失败" : 
            fieldErrors.get(0).getField() + ": " + fieldErrors.get(0).getMessage();
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(message));
    }
    
    /**
     * 处理约束违反异常
     * 
     * @param ex 约束违反异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("约束验证失败: {}", ex.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            fieldErrors.add(new ErrorResponse.FieldError(
                fieldName,
                violation.getInvalidValue(),
                violation.getMessage()
            ));
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "CONSTRAINT_VIOLATION",
            "约束验证失败",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        String message = fieldErrors.isEmpty() ? "约束验证失败" : 
            fieldErrors.get(0).getField() + ": " + fieldErrors.get(0).getMessage();
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(message));
    }
    
    /**
     * 处理缺少请求参数异常
     * 
     * @param ex 缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<ErrorResponse>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logger.warn("缺少请求参数: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER",
            "缺少必需的请求参数: " + ex.getParameterName(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest("缺少必需的请求参数: " + ex.getParameterName()));
    }
    
    /**
     * 处理方法参数类型不匹配异常
     * 
     * @param ex 方法参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<ErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("参数类型不匹配: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH",
            String.format("参数 '%s' 的值 '%s' 类型不正确", ex.getName(), ex.getValue()),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(String.format("参数 '%s' 的值 '%s' 类型不正确", ex.getName(), ex.getValue())));
    }
    
    /**
     * 处理HTTP消息不可读异常
     * 
     * @param ex HTTP消息不可读异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<ErrorResponse>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("HTTP消息不可读: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MESSAGE_NOT_READABLE",
            "请求体格式错误或不可读",
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest("请求体格式错误或不可读"));
    }
    
    /**
     * 处理HTTP请求方法不支持异常
     * 
     * @param ex HTTP请求方法不支持异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<ErrorResponse>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("HTTP请求方法不支持: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "METHOD_NOT_SUPPORTED",
            "不支持的HTTP请求方法: " + ex.getMethod(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(Result.error(405, "不支持的HTTP请求方法: " + ex.getMethod()));
    }
    
    /**
     * 处理找不到处理器异常
     * 
     * @param ex 找不到处理器异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<ErrorResponse>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logger.warn("找不到处理器: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NO_HANDLER_FOUND",
            "请求的资源不存在",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.notFound("请求的资源不存在"));
    }
    
    /**
     * 处理非法参数异常
     * 
     * @param ex 非法参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.warn("非法参数: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "ILLEGAL_ARGUMENT",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest()
            .body(Result.badRequest(ex.getMessage()));
    }
    
    /**
     * 处理所有未捕获的异常
     * 
     * @param ex 异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<ErrorResponse>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("未处理的异常: ", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "服务器内部错误",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.error("服务器内部错误，请稍后重试"));
    }
}