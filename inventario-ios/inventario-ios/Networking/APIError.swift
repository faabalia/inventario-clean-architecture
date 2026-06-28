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

extension APIError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .network(let err):       return err.localizedDescription
        case .serverError(let body):  return body.message
        case .decodingFailed:         return "Failed to process the server response."
        case .invalidResponse:        return "Received an unexpected response from the server."
        }
    }
}
