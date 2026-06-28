import XCTest
@testable import inventario_ios

final class ProductTests: XCTestCase {

    private let decoder = JSONDecoder.inventario
    private let encoder = JSONEncoder.inventario

    // MARK: - ProductResponse decoding

    func test_productResponse_decodesAllFields() throws {
        let json = """
        {"id":1,"sku":"LCHE-001","name":"Leche entera","description":"Leche entera 1L","minStock":10}
        """.data(using: .utf8)!

        let product = try decoder.decode(ProductResponse.self, from: json)

        XCTAssertEqual(product.id, 1)
        XCTAssertEqual(product.sku, "LCHE-001")
        XCTAssertEqual(product.name, "Leche entera")
        XCTAssertEqual(product.description, "Leche entera 1L")
        XCTAssertEqual(product.minStock, 10)
    }

    func test_productResponse_decodesWithNullDescription() throws {
        let json = """
        {"id":2,"sku":"MNTC-001","name":"Manteca","description":null,"minStock":5}
        """.data(using: .utf8)!

        let product = try decoder.decode(ProductResponse.self, from: json)

        XCTAssertNil(product.description)
    }

    func test_productResponse_decodesPagedResult() throws {
        let json = """
        {
            "content":[{"id":1,"sku":"LCHE-001","name":"Leche entera","description":null,"minStock":10}],
            "page":{"size":10,"number":0,"totalElements":1,"totalPages":1}
        }
        """.data(using: .utf8)!

        let page = try decoder.decode(Page<ProductResponse>.self, from: json)

        XCTAssertEqual(page.totalElements, 1)
        XCTAssertEqual(page.totalPages, 1)
        XCTAssertEqual(page.number, 0)
        XCTAssertTrue(page.first)
        XCTAssertTrue(page.last)
        XCTAssertFalse(page.empty)
        XCTAssertEqual(page.content.first?.sku, "LCHE-001")
    }

    func test_productResponse_decodesEmptyPage() throws {
        let json = """
        {"content":[],"page":{"size":10,"number":0,"totalElements":0,"totalPages":0}}
        """.data(using: .utf8)!

        let page = try decoder.decode(Page<ProductResponse>.self, from: json)

        XCTAssertTrue(page.empty)
        XCTAssertTrue(page.content.isEmpty)
    }

    // MARK: - CreateProductRequest encoding

    func test_createProductRequest_encodesAllFields() throws {
        let request = CreateProductRequest(sku: "LCHE-001", name: "Leche entera", description: "1L")
        let json = try asJSON(encoder.encode(request))

        XCTAssertEqual(json["sku"] as? String, "LCHE-001")
        XCTAssertEqual(json["name"] as? String, "Leche entera")
        XCTAssertEqual(json["description"] as? String, "1L")
    }

    // MARK: - UpdateProductRequest encoding

    func test_updateProductRequest_encodesAllFields() throws {
        let request = UpdateProductRequest(name: "Leche descremada", description: nil, minStock: 20)
        let json = try asJSON(encoder.encode(request))

        XCTAssertEqual(json["name"] as? String, "Leche descremada")
        XCTAssertEqual(json["minStock"] as? Int, 20)
    }

    // MARK: - Helpers

    private func asJSON(_ data: Data) throws -> [String: Any] {
        try JSONSerialization.jsonObject(with: data) as! [String: Any]
    }
}
