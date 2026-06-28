import Foundation

struct ProductService {
    private let client: APIClientProtocol

    init(client: APIClientProtocol = URLSessionAPIClient()) {
        self.client = client
    }

    func getProducts(page: Int = 0, size: Int = 10) async throws -> Page<ProductResponse> {
        try await client.send(try APIRouter.getProducts(page: page, size: size).makeRequest())
    }

    func createProduct(_ body: CreateProductRequest) async throws -> ProductResponse {
        try await client.send(try APIRouter.createProduct(body).makeRequest())
    }

    func getProduct(id: Int) async throws -> ProductResponse {
        try await client.send(try APIRouter.getProduct(id: id).makeRequest())
    }

    func updateProduct(id: Int, with body: UpdateProductRequest) async throws -> ProductResponse {
        try await client.send(try APIRouter.updateProduct(id: id, request: body).makeRequest())
    }

    func getStockEntries(productId: Int) async throws -> [RegisterStockEntryResponse] {
        try await client.send(try APIRouter.getStockEntries(productId: productId).makeRequest())
    }
}
