import Foundation

@MainActor
@Observable
final class ProductListViewModel {
    var products: [ProductResponse] = []
    var isLoading = false
    var errorMessage: String?
    private(set) var hasMore = false

    private var currentPage = 0
    private let pageSize = 20
    private let service: ProductService

    init(service: ProductService = ProductService()) {
        self.service = service
    }

    func loadProducts() async {
        guard !isLoading else { return }
        isLoading = true
        errorMessage = nil
        do {
            let page = try await service.getProducts(page: 0, size: pageSize)
            products = page.content
            hasMore = !page.last
            currentPage = 0
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func loadNextPage() async {
        guard !isLoading, hasMore else { return }
        isLoading = true
        do {
            let next = currentPage + 1
            let page = try await service.getProducts(page: next, size: pageSize)
            products.append(contentsOf: page.content)
            hasMore = !page.last
            currentPage = next
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
