package com.cleveronion.application.upload.usecase

import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.application.shared.port.FileStoragePort
import com.cleveronion.application.upload.command.DeleteImageCommand
import com.cleveronion.domain.shared.exception.UnauthorizedOperationException
import com.cleveronion.domain.user.aggregate.User
import com.cleveronion.domain.user.port.UserRepositoryPort
import com.cleveronion.domain.user.valueobject.GitHubId
import com.cleveronion.domain.user.valueobject.UserId
import com.cleveronion.domain.user.valueobject.UserProfile
import com.cleveronion.domain.user.valueobject.Email
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteImageUseCaseTest {
    
    private val fileStoragePort = mockk<FileStoragePort>()
    private val userRepositoryPort = mockk<UserRepositoryPort>()
    private val deleteImageUseCase = DeleteImageUseCase(fileStoragePort, userRepositoryPort)
    
    @Test
    fun `should delete image successfully when valid command provided`() = runTest {
        // Given
        val userId = 1L
        val fileUrl = "https://example.com/images/2024/01/01/test.jpg"
        
        val command = DeleteImageCommand(
            userId = userId,
            fileUrl = fileUrl
        )
        
        val user = User.create(
            githubId = GitHubId(123L),
            profile = UserProfile(
                githubLogin = "testuser",
                email = Email("test@example.com"),
                name = "Test User",
                bio = null,
                avatarUrl = null
            )
        )
        
        coEvery { userRepositoryPort.findById(UserId(userId)) } returns user
        coEvery { fileStoragePort.fileExists(fileUrl) } returns true
        coEvery { fileStoragePort.deleteFile(fileUrl) } returns true
        
        // When
        val result = deleteImageUseCase.execute(command)
        
        // Then
        assertEquals(fileUrl, result.url)
        assertTrue(result.success)
        assertEquals("File deleted successfully", result.message)
        
        coVerify { userRepositoryPort.findById(UserId(userId)) }
        coVerify { fileStoragePort.fileExists(fileUrl) }
        coVerify { fileStoragePort.deleteFile(fileUrl) }
    }
    
    @Test
    fun `should return failure when file does not exist`() = runTest {
        // Given
        val userId = 1L
        val fileUrl = "https://example.com/images/2024/01/01/nonexistent.jpg"
        
        val command = DeleteImageCommand(
            userId = userId,
            fileUrl = fileUrl
        )
        
        val user = User.create(
            githubId = GitHubId(123L),
            profile = UserProfile(
                githubLogin = "testuser",
                email = Email("test@example.com"),
                name = "Test User",
                bio = null,
                avatarUrl = null
            )
        )
        
        coEvery { userRepositoryPort.findById(UserId(userId)) } returns user
        coEvery { fileStoragePort.fileExists(fileUrl) } returns false
        
        // When
        val result = deleteImageUseCase.execute(command)
        
        // Then
        assertEquals(fileUrl, result.url)
        assertFalse(result.success)
        assertEquals("File not found", result.message)
        
        coVerify { userRepositoryPort.findById(UserId(userId)) }
        coVerify { fileStoragePort.fileExists(fileUrl) }
        coVerify(exactly = 0) { fileStoragePort.deleteFile(any()) }
    }
    
    @Test
    fun `should throw UnauthorizedOperationException when user not found`() = runTest {
        // Given
        val command = DeleteImageCommand(
            userId = 999L,
            fileUrl = "https://example.com/images/2024/01/01/test.jpg"
        )
        
        coEvery { userRepositoryPort.findById(UserId(999L)) } returns null
        
        // When & Then
        assertThrows<UnauthorizedOperationException> {
            deleteImageUseCase.execute(command)
        }
    }
    
    @Test
    fun `should throw ValidationException when file URL is blank`() = runTest {
        // Given
        val command = DeleteImageCommand(
            userId = 1L,
            fileUrl = ""
        )
        
        val user = User.create(
            githubId = GitHubId(123L),
            profile = UserProfile(
                githubLogin = "testuser",
                email = Email("test@example.com"),
                name = "Test User",
                bio = null,
                avatarUrl = null
            )
        )
        
        coEvery { userRepositoryPort.findById(UserId(1L)) } returns user
        
        // When & Then
        assertThrows<ValidationException> {
            deleteImageUseCase.execute(command)
        }
    }
    
    @Test
    fun `should throw ValidationException when file URL has invalid extension`() = runTest {
        // Given
        val command = DeleteImageCommand(
            userId = 1L,
            fileUrl = "https://example.com/files/document.pdf"
        )
        
        val user = User.create(
            githubId = GitHubId(123L),
            profile = UserProfile(
                githubLogin = "testuser",
                email = Email("test@example.com"),
                name = "Test User",
                bio = null,
                avatarUrl = null
            )
        )
        
        coEvery { userRepositoryPort.findById(UserId(1L)) } returns user
        
        // When & Then
        assertThrows<ValidationException> {
            deleteImageUseCase.execute(command)
        }
    }
    
    @Test
    fun `should return failure when storage deletion fails`() = runTest {
        // Given
        val userId = 1L
        val fileUrl = "https://example.com/images/2024/01/01/test.jpg"
        
        val command = DeleteImageCommand(
            userId = userId,
            fileUrl = fileUrl
        )
        
        val user = User.create(
            githubId = GitHubId(123L),
            profile = UserProfile(
                githubLogin = "testuser",
                email = Email("test@example.com"),
                name = "Test User",
                bio = null,
                avatarUrl = null
            )
        )
        
        coEvery { userRepositoryPort.findById(UserId(userId)) } returns user
        coEvery { fileStoragePort.fileExists(fileUrl) } returns true
        coEvery { fileStoragePort.deleteFile(fileUrl) } returns false
        
        // When
        val result = deleteImageUseCase.execute(command)
        
        // Then
        assertEquals(fileUrl, result.url)
        assertFalse(result.success)
        assertEquals("Failed to delete file", result.message)
    }
}