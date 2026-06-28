import Foundation

@MainActor
@Observable
final class ProductDetailViewModel {
    var stockEntries: [RegisterStockEntryResponse] = []
    var isLoading = false
    var errorMessage: String?

    private let service: ProductService
    private let stockEntryService: StockEntryService

    init(
        service: ProductService = ProductService(),
        stockEntryService: StockEntryService = StockEntryService()
    ) {
        self.service = service
        self.stockEntryService = stockEntryService
    }

    func loadStockEntries(productId: Int) async {
        isLoading = true
        errorMessage = nil
        do {
            stockEntries = try await service.getStockEntries(productId: productId)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func deleteStockEntry(id: Int) async {
        do {
            try await stockEntryService.deleteStockEntry(id: id)
            stockEntries.removeAll { $0.id == id }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
