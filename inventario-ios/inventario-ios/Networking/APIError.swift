import Foundation

// Maps the backend's uniform error body: {code, message, timestamp, path}
struct ErrorResponse: Codable, Sendable {
    let code: String
    let message: String
    let timestamp: Date
    let path: String
}

enum APIError: Error, Sendable {
    case network(URLError)
    case serverError(ErrorResponse)
    case decodingFailed
    case invalidResponse
}
