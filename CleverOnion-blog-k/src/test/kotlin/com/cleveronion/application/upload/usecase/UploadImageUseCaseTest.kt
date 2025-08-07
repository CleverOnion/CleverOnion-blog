package com.cleveronion.application.upload.usecase

import com.cleveronion.application.shared.exception.ValidationException
import com.cleveronion.application.shared.port.FileStoragePort
import com.cleveronion.application.upload.command.UploadImageCommand
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
import kotlin.test.assertTrue

class UploadImageUseCaseTest {
    
    private val fileStoragePort = mockk<FileStoragePort>()
    private val userRepositoryPort = mockk<UserRepositoryPort>()
    private val uploadImageUseCase = UploadImageUseCase(fileStoragePort, userRepositoryPort)
    
    @Test
    fun `should upload image successfully when valid command provided`() = runTest {
        // Given
        val userId = 1L
        val fileName = "test.jpg"
        val fileContent = "test image content".toByteArray()
        val contentType = "image/jpeg"
        val fileSize = fileContent.size.toLong()
        
        val command = UploadImageCommand(
            userId = userId,
            fileName = fileName,
            fileContent = fileContent,
            contentType = contentType,
            fileSize = fileSize
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
        
        val expectedUrl = "https://example.com/images/2024/01/01/uuid.jpg"
        
        coEvery { userRepositoryPort.findById(UserId(userId)) } returns user
        coEvery { 
            fileStoragePort.uploadFile(any(), fileContent, contentType) 
        } returns expectedUrl
        
        // When
        val result = uploadImageUseCase.execute(command)
        
        // Then
        assertEquals(fileName, result.originalFileName)
        assertEquals(expectedUrl, result.url)
        assertEquals(contentType, result.contentType)
        assertEquals(fileSize, result.fileSize)
        assertTrue(result.fileName.startsWith("images/"))
        assertTrue(result.fileName.endsWith(".jpg"))
        
        coVerify { userRepositoryPort.findById(UserId(userId)) }
        coVerify { fileStoragePort.uploadFile(any(), fileContent, contentType) }
    }
    
    @Test
    fun `should throw UnauthorizedOperationException when user not found`() = runTest {
        // Given
        val command = UploadImageCommand(
            userId = 999L,
            fileName = "test.jpg",
            fileContent = "test".toByteArray(),
            contentType = "image/jpeg",
            fileSize = 4L
        )
        
        coEvery { userRepositoryPort.findById(UserId(999L)) } returns null
        
        // When & Then
        assertThrows<UnauthorizedOperationException> {
            uploadImageUseCase.execute(command)
        }
    }
    
    @Test
    fun `should throw ValidationException when unsupported content type`() = runTest {
        // Given
        val command = UploadImageCommand(
            userId = 1L,
            fileName = "test.txt",
            fileContent = "test".toByteArray(),
            contentType = "text/plain",
            fileSize = 4L
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
            uploadImageUseCase.execute(command)
        }
    }
    
    @Test
    fun `should throw ValidationException when file size exceeds limit`() = runTest {
        // Given
        val largeContent = ByteArray(11 * 1024 * 1024) // 11MB
        val command = UploadImageCommand(
            userId = 1L,
            fileName = "large.jpg",
            fileContent = largeContent,
            contentType = "image/jpeg",
            fileSize = largeContent.size.toLong()
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
            uploadImageUseCase.execute(command)
        }
    }
}