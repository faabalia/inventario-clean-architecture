import Foundation

struct RegisterStockEntryResponse: Codable, Identifiable, Equatable, Sendable {
    let id: Int
    let productId: Int
    let quantity: Int
    let expiryDate: Date    // Java LocalDate — decoded as "yyyy-MM-dd"
    let receivedDate: Date  // Java LocalDate — decoded as "yyyy-MM-dd"
}

struct RegisterStockEntryRequest: Encodable, Sendable {
    let productId: Int
    let quantity: Int
    let expiryDate: Date    // Java LocalDate — encoded as "yyyy-MM-dd"
}
