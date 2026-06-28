import XCTest
@testable import inventario_ios

final class ProductServiceTests: XCTestCase {

    private var sut: ProductService!

    override func setUp() {
        super.setUp()
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        sut = ProductService(client: URLSessionAPIClient(session: URLSession(configuration: config)))
    }

    override func tearDown() {
        MockURLProtocol.requestHandler = nil
        sut = nil
        super.tearDown()
    }

    // MARK: - getProducts

    func test_getProducts_sendsGETWithPaginationParams() async throws {
        MockURLProtocol.requestHandler = { request in
            XCTAssertEqual(request.httpMethod, "GET")
            let url = request.url?.absoluteString ?? ""
            XCTAssertTrue(url.contains("page=1"))
            XCTAssertTrue(url.contains("size=20"))
            return (self.makeHTTPResponse(200), self.pagedProductsJSON())
        }
        _ = try await sut.getProducts(page: 1, size: 20)
    }

    func test_getProducts_decodesPagedProductList() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.pagedProductsJSON())
        }
        let page = try await sut.getProducts()
        XCTAssertEqual(page.totalElements, 1)
        XCTAssertEqual(page.content.first?.sku, "LCHE-001")
    }

    func test_getProducts_defaultsToPageZeroSize10() async throws {
        MockURLProtocol.requestHandler = { request in
            let url = request.url?.absoluteString ?? ""
            XCTAssertTrue(url.contains("page=0"))
            XCTAssertTrue(url.contains("size=10"))
            return (self.makeHTTPResponse(200), self.pagedProductsJSON())
        }
        _ = try await sut.getProducts()
    }

    // MARK: - createProduct

    func test_createProduct_sendsPOSTToProductsEndpoint() async throws {
        MockURLProtocol.requestHandler = { request in
            XCTAssertEqual(request.httpMethod, "POST")
            XCTAssertEqual(request.url?.path, "/api/products")
            XCTAssertEqual(request.value(forHTTPHeaderField: "Content-Type"), "application/json")
            guard let body = self.readBody(from: request) else {
                XCTFail("Expected request body"); return (self.makeHTTPResponse(400), nil)
            }
            let json = try JSONSerialization.jsonObject(with: body) as! [String: Any]
            XCTAssertEqual(json["sku"] as? String, "LCHE-001")
            return (self.makeHTTPResponse(201), self.productJSON())
        }
        _ = try await sut.createProduct(CreateProductRequest(sku: "LCHE-001", name: "Leche", description: nil))
    }

    func test_createProduct_decodesCreatedProduct() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(201), self.productJSON())
        }
        let product = try await sut.createProduct(CreateProductRequest(sku: "LCHE-001", name: "Leche", description: nil))
        XCTAssertEqual(product.id, 1)
        XCTAssertEqual(product.sku, "LCHE-001")
    }

    // MARK: - getStockEntries

    func test_getStockEntries_sendsGETToCorrectPath() async throws {
        MockURLProtocol.requestHandler = { request in
            XCTAssertEqual(request.httpMethod, "GET")
            XCTAssertEqual(request.url?.path, "/api/products/42/stock-entries")
            return (self.makeHTTPResponse(200), self.stockEntriesJSON())
        }
        _ = try await sut.getStockEntries(productId: 42)
    }

    func test_getStockEntries_decodesStockEntryList() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(200), self.stockEntriesJSON())
        }
        let entries = try await sut.getStockEntries(productId: 1)
        XCTAssertEqual(entries.count, 1)
        XCTAssertEqual(entries.first?.quantity, 100)
    }

    // MARK: - Fixtures & Helpers

    private func makeHTTPResponse(_ status: Int) -> HTTPURLResponse {
        HTTPURLResponse(
            url: URL(string: "http://localhost:8080/api/test")!,
            statusCode: status,
            httpVersion: nil,
            headerFields: nil
        )!
    }

    private func pagedProductsJSON() -> Data {
        """
        {"content":[{"id":1,"sku":"LCHE-001","name":"Leche entera","description":null,"minStock":10}],
        "page":{"size":10,"number":0,"totalElements":1,"totalPages":1}}
        """.data(using: .utf8)!
    }

    private func productJSON() -> Data {
        #"{"id":1,"sku":"LCHE-001","name":"Leche entera","description":null,"minStock":10}"#
            .data(using: .utf8)!
    }

    private func stockEntriesJSON() -> Data {
        #"[{"id":1,"productId":1,"quantity":100,"expiryDate":"2026-12-31","receivedDate":"2026-01-15"}]"#
            .data(using: .utf8)!
    }

    // URLSession converts httpBody → httpBodyStream before passing to URLProtocol
    private func readBody(from request: URLRequest) -> Data? {
        if let data = request.httpBody { return data }
        guard let stream = request.httpBodyStream else { return nil }
        stream.open()
        defer { stream.close() }
        var data = Data()
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: 4096)
        defer { buffer.deallocate() }
        while stream.hasBytesAvailable {
            let count = stream.read(buffer, maxLength: 4096)
            if count > 0 { data.append(buffer, count: count) }
        }
        return data
    }
}
