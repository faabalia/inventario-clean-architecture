import XCTest
@testable import inventario_ios

final class StockEntryServiceTests: XCTestCase {

    private var sut: StockEntryService!

    override func setUp() {
        super.setUp()
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        sut = StockEntryService(client: URLSessionAPIClient(session: URLSession(configuration: config)))
    }

    override func tearDown() {
        MockURLProtocol.requestHandler = nil
        sut = nil
        super.tearDown()
    }

    // MARK: - createStockEntry

    func test_createStockEntry_sendsPOSTToStockEntriesEndpoint() async throws {
        MockURLProtocol.requestHandler = { request in
            XCTAssertEqual(request.httpMethod, "POST")
            XCTAssertEqual(request.url?.path, "/api/stock-entries")
            XCTAssertEqual(request.value(forHTTPHeaderField: "Content-Type"), "application/json")
            guard let body = self.readBody(from: request) else {
                XCTFail("Expected request body"); return (self.makeHTTPResponse(400), nil)
            }
            let json = try JSONSerialization.jsonObject(with: body) as! [String: Any]
            XCTAssertEqual(json["productId"] as? Int, 1)
            XCTAssertEqual(json["quantity"] as? Int, 50)
            XCTAssertEqual(json["expiryDate"] as? String, "2026-12-31")
            return (self.makeHTTPResponse(201), self.stockEntryJSON())
        }
        _ = try await sut.createStockEntry(
            RegisterStockEntryRequest(productId: 1, quantity: 50, expiryDate: localDate("2026-12-31")!)
        )
    }

    func test_createStockEntry_decodesCreatedEntry() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(201), self.stockEntryJSON())
        }
        let entry = try await sut.createStockEntry(
            RegisterStockEntryRequest(productId: 1, quantity: 50, expiryDate: localDate("2026-12-31")!)
        )
        XCTAssertEqual(entry.id, 1)
        XCTAssertEqual(entry.quantity, 50)
    }

    // MARK: - deleteStockEntry

    func test_deleteStockEntry_sendsDELETEToCorrectPath() async throws {
        MockURLProtocol.requestHandler = { request in
            XCTAssertEqual(request.httpMethod, "DELETE")
            XCTAssertEqual(request.url?.path, "/api/stock-entries/7")
            return (self.makeHTTPResponse(204), nil)
        }
        try await sut.deleteStockEntry(id: 7)
    }

    func test_deleteStockEntry_doesNotThrowOn204() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(204), nil)
        }
        // Must not throw
        try await sut.deleteStockEntry(id: 1)
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

    private func stockEntryJSON() -> Data {
        #"{"id":1,"productId":1,"quantity":50,"expiryDate":"2026-12-31","receivedDate":"2026-06-28"}"#
            .data(using: .utf8)!
    }

    private func localDate(_ string: String) -> Date? {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.timeZone = TimeZone(secondsFromGMT: 0)
        return f.date(from: string)
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
