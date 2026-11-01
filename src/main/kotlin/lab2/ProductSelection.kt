package lab2


fun selectProducts(
    products: List<Product>,
    recommendedIds: List<Long>,
    excludedIds: List<Long>
): List<Product> {
    // n - products.size, m - recommendedIds.size, k - excludedIds,    N = max(n, m, k)
    // Сложность O(N), по памяти O(N)
    val excludedIdsSet = excludedIds.toSet()

    val productMap = products
        .filter { product -> product.available && product.id !in excludedIdsSet }
        .associateByTo(HashMap()) { it.id } // Так как все айдишинки уникальные

    val result = mutableListOf<Product>()

    for (productId in recommendedIds) {
        if (productMap.isEmpty()) break
        productMap.remove(productId)?.let { result.add(it) }
    }

    return result
}
