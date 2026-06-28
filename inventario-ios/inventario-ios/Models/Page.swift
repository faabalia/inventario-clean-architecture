import Foundation

struct Page<T: Codable>: Codable {
    let content: [T]
    // Spring Boot 3.2+ moved pagination metadata under a nested "page" key
    let page: PageMetadata

    struct PageMetadata: Codable, Sendable {
        let size: Int
        let number: Int        // 0-based current page index
        let totalElements: Int
        let totalPages: Int
    }

    // Convenience accessors so call-sites don't need to change
    var totalElements: Int { page.totalElements }
    var totalPages: Int    { page.totalPages }
    var number: Int        { page.number }
    var size: Int          { page.size }
    var first: Bool        { page.number == 0 }
    var last: Bool         { page.totalPages == 0 || page.number >= page.totalPages - 1 }
    var empty: Bool        { content.isEmpty }
}

extension Page: Sendable where T: Sendable {}
