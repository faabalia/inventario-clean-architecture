import Foundation

enum APIRouter {
    static let baseURL = URL(string: "http://localhost:8080/api")!

    // Products
    case getProducts(page: Int, size: Int)
    case createProduct(CreateProductRequest)
    case getProduct(id: Int)
    case updateProduct(id: Int, request: UpdateProductRequest)
    case getStockEntries(productId: Int)

    // Stock entries
    case createStockEntry(RegisterStockEntryRequest)
    case deleteStockEntry(id: Int)

    func makeRequest() throws -> URLRequest {
        var components = URLComponents(
            url: Self.baseURL.appendingPathComponent(path),
            resolvingAgainstBaseURL: false
        )!
        if let items = queryItems, !items.isEmpty {
            components.queryItems = items
        }
        guard let url = components.url else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = httpMethod

        if let body = try encodedBody() {
            request.httpBody = body
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        return request
    }

    // MARK: - Private

    private var path: String {
        switch self {
        case .getProducts, .createProduct:
            return "products"
        case .getProduct(let id):
            return "products/\(id)"
        case .updateProduct(let id, _):
            return "products/\(id)"
        case .getStockEntries(let productId):
            return "products/\(productId)/stock-entries"
        case .createStockEntry:
            return "stock-entries"
        case .deleteStockEntry(let id):
            return "stock-entries/\(id)"
        }
    }

    private var httpMethod: String {
        switch self {
        case .getProducts, .getProduct, .getStockEntries:
            return "GET"
        case .createProduct, .createStockEntry:
            return "POST"
        case .updateProduct:
            return "PUT"
        case .deleteStockEntry:
            return "DELETE"
        }
    }

    private var queryItems: [URLQueryItem]? {
        switch self {
        case .getProducts(let page, let size):
            return [
                URLQueryItem(name: "page", value: "\(page)"),
                URLQueryItem(name: "size", value: "\(size)")
            ]
        default:
            return nil
        }
    }

    private func encodedBody() throws -> Data? {
        let encoder = JSONEncoder.inventario
        switch self {
        case .createProduct(let body):
            return try encoder.encode(body)
        case .updateProduct(_, let body):
            return try encoder.encode(body)
        case .createStockEntry(let body):
            return try encoder.encode(body)
        default:
            return nil
        }
    }
}
