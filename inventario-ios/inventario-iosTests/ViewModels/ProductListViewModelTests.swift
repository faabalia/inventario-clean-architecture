import XCTest
@testable import inventario_ios

@MainActor
final class ProductListViewModelTests: XCTestCase {

    override func tearDown() {
        MockURLProtocol.requestHandler = nil
        super.tearDown()
    }

    // MARK: - loadProducts

    func test_loadProducts_populatesProductsOnSuccess() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.pagedProductsJSON())
        }
        let sut = ProductListViewModel(service: makeService())

        await sut.loadProducts()

        XCTAssertEqual(sut.products.count, 1)
        XCTAssertEqual(sut.products.first?.sku, "LCHE-001")
        XCTAssertFalse(sut.isLoading)
        XCTAssertNil(sut.errorMessage)
    }

    func test_loadProducts_setsErrorOnNetworkFailure() async {
        MockURLProtocol.requestHandler = { _ in throw URLError(.notConnectedToInternet) }
        let sut = ProductListViewModel(service: makeService())

        await sut.loadProducts()

        XCTAssertTrue(sut.products.isEmpty)
        XCTAssertNotNil(sut.errorMessage)
        XCTAssertFalse(sut.isLoading)
    }

    func test_loadProducts_setsHasMoreWhenNotLastPage() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.firstPageJSON())
        }
        let sut = ProductListViewModel(service: makeService())

        await sut.loadProducts()

        XCTAssertTrue(sut.hasMore)
    }

    func test_loadProducts_clearsHasMoreOnLastPage() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.pagedProductsJSON())
        }
        let sut = ProductListViewModel(service: makeService())

        await sut.loadProducts()

        XCTAssertFalse(sut.hasMore)
    }

    // MARK: - loadNextPage

    func test_loadNextPage_appendsProductsToList() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.firstPageJSON())
        }
        let sut = ProductListViewModel(service: makeService())
        await sut.loadProducts()
        XCTAssertEqual(sut.products.count, 1)

        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.secondPageJSON())
        }
        await sut.loadNextPage()

        XCTAssertEqual(sut.products.count, 2)
    }

    func test_loadNextPage_doesNothingWhenHasMoreIsFalse() async {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.pagedProductsJSON()) // last: true
        }
        let sut = ProductListViewModel(service: makeService())
        await sut.loadProducts()
        XCTAssertFalse(sut.hasMore)

        MockURLProtocol.requestHandler = { _ in
            XCTFail("Must not make a request when hasMore is false")
            throw URLError(.unknown)
        }
        await sut.loadNextPage()

        XCTAssertEqual(sut.products.count, 1)
    }

    // MARK: - Fixtures & Helpers

    private func makeService() -> ProductService {
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        return ProductService(client: URLSessionAPIClient(session: URLSession(configuration: config)))
    }

    private func makeHTTPResponse(_ status: Int) -> HTTPURLResponse {
        HTTPURLResponse(
            url: URL(string: "http://localhost:8080/api/test")!,
            statusCode: status, httpVersion: nil, headerFields: nil
        )!
    }

    private func pagedProductsJSON() -> Data {
        """
        {"content":[{"id":1,"sku":"LCHE-001","name":"Leche entera","description":null,"minStock":10}],
        "page":{"size":20,"number":0,"totalElements":1,"totalPages":1}}
        """.data(using: .utf8)!
    }

    private func firstPageJSON() -> Data {
        // number=0, totalPages=2 → last=false
        """
        {"content":[{"id":1,"sku":"LCHE-001","name":"Leche entera","description":null,"minStock":10}],
        "page":{"size":1,"number":0,"totalElements":2,"totalPages":2}}
        """.data(using: .utf8)!
    }

    private func secondPageJSON() -> Data {
        // number=1, totalPages=2 → last=true
        """
        {"content":[{"id":2,"sku":"MANT-001","name":"Manteca","description":null,"minStock":5}],
        "page":{"size":1,"number":1,"totalElements":2,"totalPages":2}}
        """.data(using: .utf8)!
    }
}
