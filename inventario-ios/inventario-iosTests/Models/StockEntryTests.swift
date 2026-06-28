import XCTest
@testable import inventario_ios

final class StockEntryTests: XCTestCase {

    private let decoder = JSONDecoder.inventario
    private let encoder = JSONEncoder.inventario

    // MARK: - RegisterStockEntryResponse decoding

    func test_stockEntryResponse_decodesAllFields() throws {
        let json = """
        {"id":1,"productId":5,"quantity":100,"expiryDate":"2026-12-31","receivedDate":"2026-01-15"}
        """.data(using: .utf8)!

        let entry = try decoder.decode(RegisterStockEntryResponse.self, from: json)

        XCTAssertEqual(entry.id, 1)
        XCTAssertEqual(entry.productId, 5)
        XCTAssertEqual(entry.quantity, 100)
    }

    func test_stockEntryResponse_decodesLocalDateAsCorrectDate() throws {
        let json = """
        {"id":1,"productId":5,"quantity":100,"expiryDate":"2026-12-31","receivedDate":"2026-01-15"}
        """.data(using: .utf8)!

        let entry = try decoder.decode(RegisterStockEntryResponse.self, from: json)

        // Critical: backend sends "yyyy-MM-dd" — must round-trip without corruption
        XCTAssertEqual(formatted(entry.expiryDate), "2026-12-31")
        XCTAssertEqual(formatted(entry.receivedDate), "2026-01-15")
    }

    // MARK: - RegisterStockEntryRequest encoding

    func test_stockEntryRequest_encodesDateAsLocalDateFormat() throws {
        let expiryDate = localDate("2026-12-31")!
        let request = RegisterStockEntryRequest(productId: 5, quantity: 100, expiryDate: expiryDate)
        let json = try asJSON(encoder.encode(request))

        XCTAssertEqual(json["productId"] as? Int, 5)
        XCTAssertEqual(json["quantity"] as? Int, 100)
        // Backend expects "yyyy-MM-dd" — must NOT encode as a timestamp
        XCTAssertEqual(json["expiryDate"] as? String, "2026-12-31")
    }

    // MARK: - Helpers

    private func formatted(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.timeZone = TimeZone(secondsFromGMT: 0)
        return f.string(from: date)
    }

    private func localDate(_ string: String) -> Date? {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.timeZone = TimeZone(secondsFromGMT: 0)
        return f.date(from: string)
    }

    private func asJSON(_ data: Data) throws -> [String: Any] {
        try JSONSerialization.jsonObject(with: data) as! [String: Any]
    }
}
