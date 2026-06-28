import Foundation

struct StockEntryService {
    private let client: APIClientProtocol

    init(client: APIClientProtocol = URLSessionAPIClient()) {
        self.client = client
    }

    func createStockEntry(_ body: RegisterStockEntryRequest) async throws -> RegisterStockEntryResponse {
        try await client.send(try APIRouter.createStockEntry(body).makeRequest())
    }

    func deleteStockEntry(id: Int) async throws {
        try await client.send(try APIRouter.deleteStockEntry(id: id).makeRequest())
    }
}
