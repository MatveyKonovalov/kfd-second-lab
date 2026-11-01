package lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class ProductSelectionTest {
    @Test
    fun selectsAvailableProductsInRecommendationOrder() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(3L, 2L, 3L, 99L, 1L, 4L),
            listOf(1L)
        )

        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun returnsEmptyListWhenThereAreNoRecommendations() {
        val products = listOf(Product(1, "Keyboard", true))

        val result = selectProducts(products, emptyList(), emptyList())

        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun `select zero products when available = false`() {
        val keyboard = Product(1, "Keyboard", false)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", false)
        val webcam = Product(4, "Webcam", false)

        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(3L, 2L, 3L, 99L, 1L, 4L),
            listOf(1L)
        )

        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun `check simple combination && maintaining order`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(2L, 3L, 4L, 99L, 1L),
            emptyList()
        )

        assertEquals(listOf(mouse, monitor, webcam, keyboard), result)
    }

    @Test
    fun `check maintaining order`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(2L, 3L, 4L, 99L, 1L),
            listOf(2L, 1L)
        )

        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun `order follows recommendations regardless of catalog order`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        // каталог в обратном порядке
        val products = listOf(webcam, monitor, mouse, keyboard)

        val result = selectProducts(
            products,
            listOf(3L, 1L, 4L, 2L),
            emptyList()
        )

        assertEquals(listOf(monitor, keyboard, webcam, mouse), result)
    }

    @Test
    fun `duplicate recommendations do not duplicate products`() {
        val monitor = Product(3, "Monitor", true)
        val result = selectProducts(
            listOf(monitor),
            listOf(3L, 3L, 3L, 3L),
            emptyList()
        )
        assertEquals(listOf(monitor), result)
    }

    @Test
    fun `duplicate excluded ids do not change result`() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        val result = selectProducts(
            listOf(keyboard, monitor),
            listOf(1L, 3L),
            listOf(1L, 1L, 1L, 1L)
        )
        assertEquals(listOf(monitor), result)
    }

    @Test
    fun `unknown ids in recommendations and exclusions are ignored`() {
        val keyboard = Product(1, "Keyboard", true)
        val result = selectProducts(
            listOf(keyboard),
            listOf(99L, 1L, 100L),
            listOf(200L, 300L)
        )
        assertEquals(listOf(keyboard), result)
    }

    @Test
    fun `empty catalog yields empty result`() {
        val result = selectProducts(
            emptyList(),
            listOf(1L, 2L, 3L),
            listOf(1L)
        )
        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun `empty exclusions and recommendations are handled`() {
        val keyboard = Product(1, "Keyboard", true)
        assertEquals(
            emptyList<Product>(),
            selectProducts(listOf(keyboard), emptyList(), emptyList())
        )
        assertEquals(
            listOf(keyboard),
            selectProducts(listOf(keyboard), listOf(1L), emptyList())
        )
    }

    @Test
    fun `no products match when all are excluded`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val result = selectProducts(
            listOf(keyboard, mouse),
            listOf(1L, 2L),
            listOf(1L, 2L)
        )
        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun `no products match when recommendations only contain unknown ids`() {
        val keyboard = Product(1, "Keyboard", true)
        val result = selectProducts(
            listOf(keyboard),
            listOf(42L, 43L),
            emptyList()
        )
        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun `input collections are not modified`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val products = listOf(keyboard, mouse)
        val recommended = mutableListOf(1L, 2L, 1L)
        val excluded = mutableListOf(2L, 2L)

        val productsCopy = products.toList()
        val recommendedCopy = recommended.toList()
        val excludedCopy = excluded.toList()

        selectProducts(products, recommended, excluded)

        assertEquals(productsCopy, products)
        assertEquals(recommendedCopy, recommended)
        assertEquals(excludedCopy, excluded)
    }

    @Test
    fun `repeated calls return identical results and do not share state`() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val monitor = Product(3, "Monitor", true)
        val products = listOf(keyboard, mouse, monitor)
        val recommended = listOf(3L, 1L, 2L)

        val first = selectProducts(products, recommended, emptyList())
        val second = selectProducts(products, recommended, emptyList())

        assertEquals(first, second)
        assertEquals(listOf(monitor, keyboard, mouse), first)
        // списки — разные объекты, изменение одного не влияет на другой
        assertNotSame(first, second)
    }

    @Test
    fun `mutating result does not affect subsequent calls`() {
        val keyboard = Product(1, "Keyboard", true)
        val products = listOf(keyboard)

        val first = selectProducts(products, listOf(1L), emptyList())
        (first as MutableList).clear()   // портим первый результат

        val second = selectProducts(products, listOf(1L), emptyList())
        assertEquals(listOf(keyboard), second)
    }
}
