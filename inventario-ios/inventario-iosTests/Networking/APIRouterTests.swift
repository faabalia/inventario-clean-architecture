import XCTest
@testable import inventario_ios

final class APIRouterTests: XCTestCase {

    // MARK: - GET /products

    func test_getProducts_buildsCorrectURLWithQueryParams() throws {
        let request = try APIRouter.getProducts(page: 2, size: 20).makeRequest()

        let components = URLComponents(url: request.url!, resolvingAgainstBaseURL: false)!
        XCTAssertEqual(request.httpMethod, "GET")
        XCTAssertEqual(components.path, "/api/products")
        XCTAssertEqual(components.queryItems?.first(where: { $0.name == "page" })?.value, "2")
        XCTAssertEqual(components.queryItems?.first(where: { $0.name == "size" })?.value, "20")
    }

    func test_getProducts_hasNoBody() throws {
        let request = try APIRouter.getProducts(page: 0, size: 10).makeRequest()
        XCTAssertNil(request.httpBody)
    }

    // MARK: - POST /products

    func test_createProduct_usesPOSTToProductsEndpoint() throws {
        let body = CreateProductRequest(sku: "SKU-001", name: "Leche", description: nil)
        let request = try APIRouter.createProduct(body).makeRequest()

        XCTAssertEqual(request.httpMethod, "POST")
        XCTAssertEqual(request.url?.path, "/api/products")
        XCTAssertEqual(request.value(forHTTPHeaderField: "Content-Type"), "application/json")
    }

    func test_createProduct_encodesBodyAsJSON() throws {
        let body = CreateProductRequest(sku: "SKU-001", name: "Leche", description: "Entera 1L")
        let request = try APIRouter.createProduct(body).makeRequest()

        let json = try JSONSerialization.jsonObject(with: request.httpBody!) as! [String: Any]
        XCTAssertEqual(json["sku"] as? String, "SKU-001")
        XCTAssertEqual(json["name"] as? String, "Leche")
        XCTAssertEqual(json["description"] as? String, "Entera 1L")
    }

    // MARK: - GET /products/{id}/stock-entries

    func test_getStockEntries_buildsCorrectPathWithProductId() throws {
        let request = try APIRouter.getStockEntries(productId: 42).makeRequest()

        XCTAssertEqual(request.httpMethod, "GET")
        XCTAssertEqual(request.url?.path, "/api/products/42/stock-entries")
        XCTAssertNil(request.httpBody)
    }

    // MARK: - POST /stock-entries

    func test_createStockEntry_usesPOSTToStockEntriesEndpoint() throws {
        let body = RegisterStockEntryRequest(productId: 5, quantity: 100, expiryDate: localDate("2026-12-31")!)
        let request = try APIRouter.createStockEntry(body).makeRequest()

        XCTAssertEqual(request.httpMethod, "POST")
        XCTAssertEqual(request.url?.path, "/api/stock-entries")
        XCTAssertEqual(request.value(forHTTPHeaderField: "Content-Type"), "application/json")
    }

    func test_createStockEntry_encodesDateAsLocalDateFormat() throws {
        let body = RegisterStockEntryRequest(productId: 5, quantity: 100, expiryDate: localDate("2026-12-31")!)
        let request = try APIRouter.createStockEntry(body).makeRequest()

        let json = try JSONSerialization.jsonObject(with: request.httpBody!) as! [String: Any]
        XCTAssertEqual(json["productId"] as? Int, 5)
        XCTAssertEqual(json["quantity"] as? Int, 100)
        XCTAssertEqual(json["expiryDate"] as? String, "2026-12-31")
    }

    // MARK: - DELETE /stock-entries/{id}

    func test_deleteStockEntry_usesDELETEWithIdInPath() throws {
        let request = try APIRouter.deleteStockEntry(id: 7).makeRequest()

        XCTAssertEqual(request.httpMethod, "DELETE")
        XCTAssertEqual(request.url?.path, "/api/stock-entries/7")
        XCTAssertNil(request.httpBody)
    }

    // MARK: - PUT /products/{id}

    func test_updateProduct_usesPUTWithIdInPath() throws {
        let body = UpdateProductRequest(name: "Leche descremada", description: nil, minStock: 20)
        let request = try APIRouter.updateProduct(id: 1, request: body).makeRequest()

        XCTAssertEqual(request.httpMethod, "PUT")
        XCTAssertEqual(request.url?.path, "/api/products/1")

        let json = try JSONSerialization.jsonObject(with: request.httpBody!) as! [String: Any]
        XCTAssertEqual(json["name"] as? String, "Leche descremada")
        XCTAssertEqual(json["minStock"] as? Int, 20)
    }

    // MARK: - GET /products/{id}

    func test_getProduct_buildsCorrectPathWithId() throws {
        let request = try APIRouter.getProduct(id: 99).makeRequest()

        XCTAssertEqual(request.httpMethod, "GET")
        XCTAssertEqual(request.url?.path, "/api/products/99")
        XCTAssertNil(request.httpBody)
    }

    // MARK: - Helper

    private func localDate(_ string: String) -> Date? {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.timeZone = TimeZone(secondsFromGMT: 0)
        return f.date(from: string)
    }
}
