import Foundation

struct ProductResponse: Codable, Identifiable, Equatable, Sendable {
    let id: Int
    let sku: String
    let name: String
    let description: String?
    let minStock: Int
}

struct CreateProductRequest: Encodable, Sendable {
    let sku: String
    let name: String
    let description: String?
}

struct UpdateProductRequest: Encodable, Sendable {
    let name: String
    let description: String?
    let minStock: Int
}
