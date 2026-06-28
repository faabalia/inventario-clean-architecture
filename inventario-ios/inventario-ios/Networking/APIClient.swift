import Foundation

protocol APIClientProtocol {
    func send<T: Decodable>(_ request: URLRequest) async throws -> T
    func send(_ request: URLRequest) async throws
}

final class URLSessionAPIClient: APIClientProtocol {
    private let session: URLSession
    private let decoder: JSONDecoder

    init(session: URLSession = .shared) {
        self.session = session
        decoder = .inventario
    }

    func send<T: Decodable>(_ request: URLRequest) async throws -> T {
        do {
            let (data, response) = try await session.data(for: request)
            try validate(response, data: data)
            return try decode(T.self, from: data)
        } catch let error as APIError {
            throw error
        } catch let error as URLError {
            throw APIError.network(error)
        }
    }

    func send(_ request: URLRequest) async throws {
        do {
            let (data, response) = try await session.data(for: request)
            try validate(response, data: data)
        } catch let error as APIError {
            throw error
        } catch let error as URLError {
            throw APIError.network(error)
        }
    }

    private func validate(_ response: URLResponse, data: Data) throws {
        guard let http = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            if let body = try? decoder.decode(ErrorResponse.self, from: data) {
                throw APIError.serverError(body)
            }
            throw APIError.invalidResponse
        }
    }

    private func decode<T: Decodable>(_ type: T.Type, from data: Data) throws -> T {
        do {
            return try decoder.decode(type, from: data)
        } catch {
            throw APIError.decodingFailed
        }
    }
}
