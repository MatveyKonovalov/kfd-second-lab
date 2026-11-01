package lab2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProductSelectionServiceTest {

    private val keyboard = Product(1L, "Keyboard", true)
    private val mouse = Product(2L, "Mouse", false) // недоступен
    private val monitor = Product(3L, "Monitor", true)
    private val webcam = Product(4L, "Webcam", true)
    private val laptop = Product(5L, "Laptop", true)

    private val catalog = listOf(keyboard, mouse, monitor, webcam, laptop)


    @Test
    fun `selects available products in recommendation order`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(3L, 2L, 3L, 99L, 1L, 4L),
            excludedIds = listOf(1L),
            limit = 10
        )
        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun `returns empty list when limit is zero`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(1L, 2L, 3L),
            excludedIds = emptyList(),
            limit = 0
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns only first limit items`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(3L, 4L, 1L), // Monitor, Webcam, Keyboard
            excludedIds = emptyList(),
            limit = 2
        )
        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun `returns all matching products if they are fewer than limit`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(3L, 4L),
            excludedIds = emptyList(),
            limit = 100
        )
        assertEquals(listOf(monitor, webcam), result)
    }


    @Test
    fun `early exit stops processing once limit is reached`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(3L, 4L, 5L),
            excludedIds = emptyList(),
            limit = 1
        )
        assertEquals(listOf(monitor), result)
    }


    @Test
    fun `multiple requests to the same catalog do not affect each other`() {
        val service = ProductSelectionService(catalog)

        // Первый запрос
        val result1 = service.selectProducts(
            recommendedIds = listOf(3L, 4L),
            excludedIds = emptyList(),
            limit = 10
        )
        // Второй запрос с другими параметрами
        val result2 = service.selectProducts(
            recommendedIds = listOf(1L, 3L),
            excludedIds = listOf(4L),
            limit = 10
        )
        // Третий запрос, повторяющий первый (каталог не должен быть "испорчен" удалением элементов)
        val result3 = service.selectProducts(
            recommendedIds = listOf(3L, 4L),
            excludedIds = emptyList(),
            limit = 10
        )

        assertEquals(listOf(monitor, webcam), result1)
        assertEquals(listOf(keyboard, monitor), result2)
        assertEquals(listOf(monitor, webcam), result3)
    }

    @Test
    fun `exclusions from one request do not leak into another`() {
        val service = ProductSelectionService(catalog)

        service.selectProducts(listOf(1L, 3L), listOf(1L, 3L), limit = 10) // Исключаем всё
        val result = service.selectProducts(listOf(1L, 3L), emptyList(), limit = 10) // Ничего не исключаем

        assertEquals(listOf(keyboard, monitor), result)
    }

    @Test
    fun `duplicate recommendation ids are not duplicated in result`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(3L, 3L, 3L),
            excludedIds = emptyList(),
            limit = 10
        )
        assertEquals(listOf(monitor), result)
    }

    @Test
    fun `duplicate excluded ids do not break logic`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(1L, 3L),
            excludedIds = listOf(1L, 1L, 1L),
            limit = 10
        )
        assertEquals(listOf(monitor), result)
    }


    @Test
    fun `ignores unknown and unavailable products`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(99L, 2L, 3L),
            excludedIds = emptyList(),
            limit = 10
        )
        assertEquals(listOf(monitor), result)
    }

    @Test
    fun `empty recommendations returns empty result`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(emptyList(), emptyList(), limit = 10)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty catalog returns empty result`() {
        val service = ProductSelectionService(emptyList())
        val result = service.selectProducts(listOf(1L, 2L), emptyList(), limit = 10)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `no matching products returns empty result`() {
        val service = ProductSelectionService(catalog)
        val result = service.selectProducts(
            recommendedIds = listOf(99L, 100L), // только неизвестные
            excludedIds = emptyList(),
            limit = 10
        )
        assertTrue(result.isEmpty())
    }
}