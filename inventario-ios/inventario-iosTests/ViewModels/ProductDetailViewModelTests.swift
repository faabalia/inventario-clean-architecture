import XCTest
@testable import inventario_ios

@MainActor
final class ProductDetailViewModelTests: XCTestCase {

    override func tearDown() {
        MockURLProtocol.requestHandler = nil
        super.tearDown()
    }

    // MARK: - loadStockEntries

    func test_loadStockEntries_populatesEntriesOnSuccess() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.stockEntriesJSON())
        }
        let sut = ProductDetailViewModel(service: makeProductService())

        await sut.loadStockEntries(productId: 1)

        XCTAssertEqual(sut.stockEntries.count, 1)
        XCTAssertEqual(sut.stockEntries.first?.quantity, 100)
        XCTAssertFalse(sut.isLoading)
        XCTAssertNil(sut.errorMessage)
    }

    func test_loadStockEntries_setsErrorOnFailure() async {
        MockURLProtocol.requestHandler = { _ in throw URLError(.timedOut) }
        let sut = ProductDetailViewModel(service: makeProductService())

        await sut.loadStockEntries(productId: 1)

        XCTAssertTrue(sut.stockEntries.isEmpty)
        XCTAssertNotNil(sut.errorMessage)
        XCTAssertFalse(sut.isLoading)
    }

    // MARK: - deleteStockEntry

    func test_deleteStockEntry_removesEntryFromList() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.stockEntriesJSON())
        }
        let sut = ProductDetailViewModel(
            service: makeProductService(),
            stockEntryService: makeStockEntryService()
        )
        await sut.loadStockEntries(productId: 1)
        XCTAssertEqual(sut.stockEntries.count, 1)

        MockURLProtocol.requestHandler = { _ in (self.makeHTTPResponse(204), nil) }
        await sut.deleteStockEntry(id: 1)

        XCTAssertTrue(sut.stockEntries.isEmpty)
        XCTAssertNil(sut.errorMessage)
    }

    func test_deleteStockEntry_setsErrorAndKeepsListOnFailure() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.stockEntriesJSON())
        }
        let sut = ProductDetailViewModel(
            service: makeProductService(),
            stockEntryService: makeStockEntryService()
        )
        await sut.loadStockEntries(productId: 1)

        MockURLProtocol.requestHandler = { _ in throw URLError(.notConnectedToInternet) }
        await sut.deleteStockEntry(id: 1)

        XCTAssertEqual(sut.stockEntries.count, 1)
        XCTAssertNotNil(sut.errorMessage)
    }

    // MARK: - Helpers

    private func makeProductService() -> ProductService {
        ProductService(client: URLSessionAPIClient(session: makeSession()))
    }

    private func makeStockEntryService() -> StockEntryService {
        StockEntryService(client: URLSessionAPIClient(session: makeSession()))
    }

    private func makeSession() -> URLSession {
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        return URLSession(configuration: config)
    }

    private func makeHTTPResponse(_ status: Int) -> HTTPURLResponse {
        HTTPURLResponse(
            url: URL(string: "http://localhost:8080/api/test")!,
            statusCode: status, httpVersion: nil, headerFields: nil
        )!
    }

    private func stockEntriesJSON() -> Data {
        #"[{"id":1,"productId":1,"quantity":100,"expiryDate":"2026-12-31","receivedDate":"2026-01-15"}]"#
            .data(using: .utf8)!
    }
}
