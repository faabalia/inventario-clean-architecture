import XCTest
@testable import inventario_ios

final class APIClientTests: XCTestCase {

    private var sut: URLSessionAPIClient!

    override func setUp() {
        super.setUp()
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        sut = URLSessionAPIClient(session: URLSession(configuration: config))
    }

    override func tearDown() {
        MockURLProtocol.requestHandler = nil
        sut = nil
        super.tearDown()
    }

    // MARK: - 2xx success

    func test_send_whenStatus200_decodesResponseBody() async throws {
        let expected = TestPayload(id: 1, name: "Leche")
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 200), try JSONEncoder().encode(expected))
        }

        let result: TestPayload = try await sut.send(makeRequest())

        XCTAssertEqual(result, expected)
    }

    func test_send_whenStatus201_decodesResponseBody() async throws {
        let expected = TestPayload(id: 2, name: "Manteca")
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 201), try JSONEncoder().encode(expected))
        }

        let result: TestPayload = try await sut.send(makeRequest())

        XCTAssertEqual(result, expected)
    }

    func test_sendVoid_whenStatus204_doesNotThrow() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 204), nil)
        }

        // Must not throw — DELETE /stock-entries/{id} returns 204
        try await sut.send(makeRequest())
    }

    // MARK: - Server error mapping

    func test_send_whenStatus404_throwsServerErrorWithCorrectFields() async throws {
        let errorBody = makeErrorResponse(code: "PRODUCT_NOT_FOUND", path: "/api/products/99")
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 404), try self.encodeError(errorBody))
        }

        do {
            let _: TestPayload = try await sut.send(makeRequest())
            XCTFail("Expected APIError.serverError")
        } catch let error as APIError {
            guard case .serverError(let body) = error else {
                return XCTFail("Expected .serverError, got \(error)")
            }
            XCTAssertEqual(body.code, "PRODUCT_NOT_FOUND")
            XCTAssertEqual(body.path, "/api/products/99")
        }
    }

    func test_send_whenStatus500_throwsServerError() async throws {
        let errorBody = makeErrorResponse(code: "INTERNAL_ERROR", path: "/api/products")
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 500), try self.encodeError(errorBody))
        }

        do {
            let _: TestPayload = try await sut.send(makeRequest())
            XCTFail("Expected APIError.serverError")
        } catch let error as APIError {
            guard case .serverError(let body) = error else {
                return XCTFail("Expected .serverError, got \(error)")
            }
            XCTAssertEqual(body.code, "INTERNAL_ERROR")
        }
    }

    func test_send_whenErrorBodyUnparseable_throwsInvalidResponse() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 503), "plain text error".data(using: .utf8))
        }

        do {
            let _: TestPayload = try await sut.send(makeRequest())
            XCTFail("Expected APIError.invalidResponse")
        } catch APIError.invalidResponse {
            // expected — backend returned non-JSON error body
        } catch {
            XCTFail("Unexpected error type: \(error)")
        }
    }

    // MARK: - Decoding errors

    func test_send_whenResponseBodyIsMalformedJSON_throwsDecodingFailed() async throws {
        MockURLProtocol.requestHandler = { _ in
            (self.makeHTTPResponse(status: 200), "{ not valid json }".data(using: .utf8))
        }

        do {
            let _: TestPayload = try await sut.send(makeRequest())
            XCTFail("Expected APIError.decodingFailed")
        } catch APIError.decodingFailed {
            // expected
        } catch {
            XCTFail("Unexpected error type: \(error)")
        }
    }

    // MARK: - Network errors

    func test_send_whenNetworkFails_throwsWrappedURLError() async throws {
        MockURLProtocol.requestHandler = { _ in
            throw URLError(.notConnectedToInternet)
        }

        do {
            let _: TestPayload = try await sut.send(makeRequest())
            XCTFail("Expected APIError.network")
        } catch let error as APIError {
            guard case .network(let urlError) = error else {
                return XCTFail("Expected .network, got \(error)")
            }
            XCTAssertEqual(urlError.code, .notConnectedToInternet)
        }
    }

    // MARK: - Helpers

    private func makeRequest() -> URLRequest {
        URLRequest(url: URL(string: "http://localhost:8080/api/test")!)
    }

    private func makeHTTPResponse(status: Int) -> HTTPURLResponse {
        HTTPURLResponse(
            url: URL(string: "http://localhost:8080/api/test")!,
            statusCode: status,
            httpVersion: nil,
            headerFields: nil
        )!
    }

    private func makeErrorResponse(code: String, path: String) -> ErrorResponse {
        ErrorResponse(code: code, message: "Error message", timestamp: Date(), path: path)
    }

    private func encodeError(_ body: ErrorResponse) throws -> Data {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        return try encoder.encode(body)
    }
}

// MARK: - Test fixture

private struct TestPayload: Codable, Equatable, Sendable {
    let id: Int
    let name: String
}
