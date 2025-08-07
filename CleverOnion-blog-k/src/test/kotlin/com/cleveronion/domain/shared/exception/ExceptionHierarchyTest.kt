package com.cleveronion.domain.shared.exception

import com.cleveronion.application.shared.exception.ApplicationException
import com.cleveronion.application.shared.exception.ResourceNotFoundException
import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.common.exception.ConfigurationException
import com.cleveronion.common.exception.DatabaseException
import com.cleveronion.common.exception.ExceptionUtil
import com.cleveronion.common.exception.ExternalServiceException
import com.cleveronion.common.exception.InfrastructureException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExceptionHierarchyTest {
    
    @Test
    fun `DomainException should be base class for domain exceptions`() {
        val exception = BusinessRuleViolationException("Test rule violation")
        assertTrue(exception is DomainException)
        assertTrue(exception is RuntimeException)
    }
    
    @Test
    fun `EntityNotFoundException should provide factory methods`() {
        val exception = EntityNotFoundException.of("Article", 123)
        assertEquals("Article with id '123' not found", exception.message)
        
        val conditionException = EntityNotFoundException.of("User", "email = 'test@example.com'")
        assertEquals("User not found with condition: email = 'test@example.com'", conditionException.message)
    }
    
    @Test
    fun `BusinessRuleViolationException should provide factory methods`() {
        val exception = BusinessRuleViolationException.of("Article must have title")
        assertEquals("Business rule violation: Article must have title", exception.message)
        
        val detailedException = BusinessRuleViolationException.of("Article must have title", "Title is empty")
        assertEquals("Business rule violation: Article must have title. Details: Title is empty", detailedException.message)
    }
    
    @Test
    fun `UnauthorizedOperationException should provide factory methods`() {
        val exception = UnauthorizedOperationException.of("delete article", 123)
        assertEquals("User '123' is not authorized to perform operation: delete article", exception.message)
        
        val simpleException = UnauthorizedOperationException.of("admin operation")
        assertEquals("Not authorized to perform operation: admin operation", simpleException.message)
    }
    
    @Test
    fun `ConcurrencyException should provide factory methods`() {
        val exception = ConcurrencyException.versionConflict("Article", 123, 5, 7)
        assertEquals("Version conflict for Article with id '123': expected version 5, but actual version is 7", exception.message)
        
        val simpleException = ConcurrencyException.of("Article", 123)
        assertEquals("Article with id '123' has been modified by another process", simpleException.message)
    }
    
    @Test
    fun `ApplicationException should be base class for application exceptions`() {
        val exception = ValidationException("Test validation error")
        assertTrue(exception is ApplicationException)
        assertTrue(exception is RuntimeException)
    }
    
    @Test
    fun `ValidationException should provide factory methods`() {
        val fieldException = ValidationException.of("email", "Invalid format")
        assertEquals("Validation failed for field 'email': Invalid format", fieldException.message)
        assertEquals("email", fieldException.field)
        
        val violations = mapOf("email" to listOf("Invalid format"), "name" to listOf("Required"))
        val multiException = ValidationException.of(violations)
        assertTrue(multiException.message?.contains("Invalid format") ?: false)
        assertTrue(multiException.message?.contains("Required") ?: false)
    }
    
    @Test
    fun `ResourceNotFoundException should provide factory methods`() {
        val exception = ResourceNotFoundException.of("Article", "123")
        assertEquals("Article with id '123' not found", exception.message)
        assertEquals("Article", exception.resourceType)
        assertEquals("123", exception.resourceId)
    }
    
    @Test
    fun `InfrastructureException should be base class for infrastructure exceptions`() {
        val exception = DatabaseException("Test database error")
        assertTrue(exception is InfrastructureException)
        assertTrue(exception is RuntimeException)
    }
    
    @Test
    fun `DatabaseException should provide factory methods`() {
        val exception = DatabaseException.of("SELECT", "Connection timeout")
        assertEquals("Database operation 'SELECT' failed: Connection timeout", exception.message)
        assertEquals("SELECT", exception.operation)
        
        val connectionException = DatabaseException.connectionFailed(RuntimeException("Network error"))
        assertEquals("Failed to connect to database", connectionException.message)
        assertEquals("connection", connectionException.operation)
    }
    
    @Test
    fun `ExternalServiceException should provide factory methods`() {
        val exception = ExternalServiceException.of("GitHub API", "Rate limit exceeded", 429)
        assertEquals("External service 'GitHub API' call failed with status 429: Rate limit exceeded", exception.message)
        assertEquals("GitHub API", exception.serviceName)
        assertEquals(429, exception.statusCode)
        
        val unavailableException = ExternalServiceException.serviceUnavailable("Payment Service")
        assertEquals("External service 'Payment Service' is unavailable", unavailableException.message)
        assertEquals(503, unavailableException.statusCode)
    }
    
    @Test
    fun `ConfigurationException should provide factory methods`() {
        val missingException = ConfigurationException.missingConfig("database.url")
        assertEquals("Required configuration 'database.url' is missing", missingException.message)
        assertEquals("database.url", missingException.configKey)
        
        val invalidException = ConfigurationException.invalidConfig("port", "abc", "Must be a number")
        assertEquals("Invalid configuration 'port' with value 'abc': Must be a number", invalidException.message)
        assertEquals("port", invalidException.configKey)
    }
    
    @Test
    fun `ExceptionUtil should identify business exceptions`() {
        assertTrue(ExceptionUtil.isBusinessException(BusinessRuleViolationException("test")))
        assertTrue(ExceptionUtil.isBusinessException(ValidationException("test")))
        assertFalse(ExceptionUtil.isBusinessException(DatabaseException("test")))
    }
    
    @Test
    fun `ExceptionUtil should identify technical exceptions`() {
        assertTrue(ExceptionUtil.isTechnicalException(DatabaseException("test")))
        assertTrue(ExceptionUtil.isTechnicalException(ExternalServiceException("test", "service")))
        assertFalse(ExceptionUtil.isTechnicalException(BusinessRuleViolationException("test")))
    }
    
    @Test
    fun `ExceptionUtil should find root cause`() {
        val rootCause = RuntimeException("Root cause")
        val middleCause = IllegalStateException("Middle cause", rootCause)
        val topException = BusinessRuleViolationException("Top exception", middleCause)
        
        val foundRootCause = ExceptionUtil.getRootCause(topException)
        assertEquals(rootCause, foundRootCause)
    }
    
    @Test
    fun `ExceptionUtil should collect all messages`() {
        val rootCause = RuntimeException("Root cause")
        val middleCause = IllegalStateException("Middle cause", rootCause)
        val topException = BusinessRuleViolationException("Top exception", middleCause)
        
        val messages = ExceptionUtil.getAllMessages(topException)
        assertEquals(3, messages.size)
        assertEquals("Top exception", messages[0])
        assertEquals("Middle cause", messages[1])
        assertEquals("Root cause", messages[2])
    }
    
    @Test
    fun `ExceptionUtil should find specific exception type`() {
        val rootCause = RuntimeException("Root cause")
        val middleCause = IllegalStateException("Middle cause", rootCause)
        val topException = BusinessRuleViolationException("Top exception", middleCause)
        
        val foundException = ExceptionUtil.findException<IllegalStateException>(topException)
        assertNotNull(foundException)
        assertEquals("Middle cause", foundException.message)
        
        assertTrue(ExceptionUtil.containsException<RuntimeException>(topException))
        assertFalse(ExceptionUtil.containsException<ValidationException>(topException))
    }
}