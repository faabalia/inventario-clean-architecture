import Foundation

extension JSONDecoder {
    /// Decoder for the Inventario backend.
    /// Handles Java Instant ("2026-12-31T10:15:30Z") and LocalDate ("2026-12-31").
    static var inventario: JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let container = try decoder.singleValueContainer()
            let string = try container.decode(String.self)

            // Full ISO-8601 with time (Java Instant)
            if let date = ISO8601DateFormatter().date(from: string) {
                return date
            }

            // Date-only (Java LocalDate: "yyyy-MM-dd")
            let localDateFormatter = DateFormatter()
            localDateFormatter.dateFormat = "yyyy-MM-dd"
            localDateFormatter.locale = Locale(identifier: "en_US_POSIX")
            localDateFormatter.timeZone = TimeZone(secondsFromGMT: 0)
            if let date = localDateFormatter.date(from: string) {
                return date
            }

            throw DecodingError.dataCorruptedError(
                in: container,
                debugDescription: "Cannot decode date from: \(string)"
            )
        }
        return decoder
    }
}

extension JSONEncoder {
    /// Encoder for Inventario request bodies.
    /// Encodes Date as "yyyy-MM-dd" (Java LocalDate format).
    static var inventario: JSONEncoder {
        let encoder = JSONEncoder()
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        encoder.dateEncodingStrategy = .formatted(formatter)
        return encoder
    }
}
