package lab2

class ProductSelectionService(products: List<Product>) {

    private val hashTableProducts = products.filter { it.available }.associateBy { it.id }

    fun selectProducts(
        recommendedIds: List<Long>,
        excludedIds: List<Long>,
        limit: Int
    ): List<Product> {
        val excludedIdsSet = excludedIds.toSet()

        val result = mutableListOf<Product>()
        val addedElements = mutableSetOf<Long>()

        for (productId in recommendedIds) {
            val sizeAdded = addedElements.size
            if (sizeAdded >= limit) break

            if (productId !in addedElements && productId !in excludedIdsSet) {
                hashTableProducts[productId]?.let { result.add(it); addedElements.add(it.id) }
            }
        }

        return result
    }
}