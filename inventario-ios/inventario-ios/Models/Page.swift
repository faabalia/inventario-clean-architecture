import Foundation

struct Page<T: Codable>: Codable {
    let content: [T]
    let totalElements: Int
    let totalPages: Int
    let number: Int      // 0-based current page index
    let size: Int
    let first: Bool
    let last: Bool
    let empty: Bool
}

extension Page: Sendable where T: Sendable {}
