package com.cleveronion.domain.shared.valueobject

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedValueObjectsTest {
    
    @Test
    fun `CreatedAt should be created with current time`() {
        val createdAt = CreatedAt.now()
        assertTrue(createdAt.value.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(createdAt.value.isAfter(Instant.now().minusSeconds(1)))
    }
    
    @Test
    fun `CreatedAt should not allow future time`() {
        val futureTime = Instant.now().plusSeconds(120)
        assertThrows<IllegalArgumentException> {
            CreatedAt(futureTime)
        }
    }
    
    @Test
    fun `UpdatedAt should be created with current time`() {
        val updatedAt = UpdatedAt.now()
        assertTrue(updatedAt.value.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(updatedAt.value.isAfter(Instant.now().minusSeconds(1)))
    }
    
    @Test
    fun `UpdatedAt should not allow future time`() {
        val futureTime = Instant.now().plusSeconds(120)
        assertThrows<IllegalArgumentException> {
            UpdatedAt(futureTime)
        }
    }
    
    @Test
    fun `UpdatedAt should be after CreatedAt`() {
        val createdAt = CreatedAt.now()
        Thread.sleep(1) // Ensure different timestamps
        val updatedAt = UpdatedAt.now()
        
        assertTrue(updatedAt.isAfterCreation(createdAt))
    }
    
    @Test
    fun `Pagination should have valid page and pageSize`() {
        val pagination = Pagination(1, 20)
        assertEquals(1, pagination.page)
        assertEquals(20, pagination.pageSize)
    }
    
    @Test
    fun `Pagination should not allow invalid page number`() {
        assertThrows<IllegalArgumentException> {
            Pagination(0, 20)
        }
        assertThrows<IllegalArgumentException> {
            Pagination(-1, 20)
        }
    }
    
    @Test
    fun `Pagination should not allow invalid page size`() {
        assertThrows<IllegalArgumentException> {
            Pagination(1, 0)
        }
        assertThrows<IllegalArgumentException> {
            Pagination(1, -1)
        }
        assertThrows<IllegalArgumentException> {
            Pagination(1, 101)
        }
    }
    
    @Test
    fun `Pagination should calculate offset correctly`() {
        val pagination = Pagination(3, 10)
        assertEquals(20, pagination.getOffset())
    }
    
    @Test
    fun `Pagination should calculate total pages correctly`() {
        val pagination = Pagination(1, 10)
        assertEquals(3, pagination.getTotalPages(25))
        assertEquals(1, pagination.getTotalPages(0))
        assertEquals(1, pagination.getTotalPages(5))
    }
    
    @Test
    fun `Pagination should identify first and last page correctly`() {
        val firstPage = Pagination(1, 10)
        assertTrue(firstPage.isFirstPage())
        assertFalse(firstPage.isLastPage(25))
        
        val lastPage = Pagination(3, 10)
        assertFalse(lastPage.isFirstPage())
        assertTrue(lastPage.isLastPage(25))
    }
    
    @Test
    fun `Version should be created with valid value`() {
        val version = Version(5)
        assertEquals(5, version.value)
    }
    
    @Test
    fun `Version should not allow negative value`() {
        assertThrows<IllegalArgumentException> {
            Version(-1)
        }
    }
    
    @Test
    fun `Version should provide initial version`() {
        val version = Version.initial()
        assertEquals(0, version.value)
        assertTrue(version.isInitial())
    }
    
    @Test
    fun `Version should provide next version`() {
        val version = Version(5)
        val nextVersion = version.next()
        assertEquals(6, nextVersion.value)
    }
    
    @Test
    fun `Version should compare correctly`() {
        val version1 = Version(1)
        val version2 = Version(2)
        
        assertTrue(version2.isNewerThan(version1))
        assertTrue(version1.isOlderThan(version2))
        assertFalse(version1.isNewerThan(version2))
        assertFalse(version2.isOlderThan(version1))
    }
}