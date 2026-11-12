package com.group2.cleancity

import com.group2.cleancity.data.CategoryRepository
import org.junit.Assert.*
import org.junit.Test

class CategoryRepositoryTest {

    @Test
    fun getCategories_returns_non_empty_list() {
        val categories = CategoryRepository.getCategories()
        assertTrue(categories.isNotEmpty())
        assertEquals(5, categories.size)
    }

    @Test
    fun getCategoryById_int_returns_correct_category() {
        val category = CategoryRepository.getCategoryById(1)
        assertNotNull(category)
        assertEquals("Uncollected Garbage", category?.name)
    }

    @Test
    fun getCategoryById_int_returns_null_for_invalid_id() {
        val category = CategoryRepository.getCategoryById(999)
        assertNull(category)
    }

    @Test
    fun getCategoryById_string_returns_correct_category() {
        val category = CategoryRepository.getCategoryById("1")
        assertNotNull(category)
        assertEquals("Uncollected Garbage", category?.name)
    }

    @Test
    fun getCategoryById_string_returns_null_for_invalid_id() {
        assertNull(CategoryRepository.getCategoryById("invalid"))
        assertNull(CategoryRepository.getCategoryById("999"))
    }
}
