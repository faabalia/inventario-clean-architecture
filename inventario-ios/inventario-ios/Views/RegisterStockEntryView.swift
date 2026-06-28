import SwiftUI

struct RegisterStockEntryView: View {
    let productId: Int
    var onCreated: (RegisterStockEntryResponse) -> Void

    @State private var quantity = 1
    @State private var expiryDate = Calendar.current.date(byAdding: .month, value: 6, to: .now) ?? .now
    @State private var isLoading = false
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss

    private let service = StockEntryService()

    var body: some View {
        NavigationStack {
            Form {
                Section("Batch Details") {
                    Stepper("Quantity: \(quantity)", value: $quantity, in: 1...9999)
                    DatePicker(
                        "Expiry Date",
                        selection: $expiryDate,
                        in: Calendar.current.startOfDay(for: .now)...,
                        displayedComponents: .date
                    )
                }
            }
            .navigationTitle("Register Stock")
            .navigationBarTitleDisplayMode(.inline)
            .disabled(isLoading)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    if isLoading {
                        ProgressView()
                    } else {
                        Button("Save") { Task { await save() } }
                    }
                }
            }
            .alert("Error", isPresented: Binding(
                get: { errorMessage != nil },
                set: { if !$0 { errorMessage = nil } }
            )) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(errorMessage ?? "")
            }
        }
    }

    private func save() async {
        isLoading = true
        errorMessage = nil
        do {
            let entry = try await service.createStockEntry(RegisterStockEntryRequest(
                productId: productId,
                quantity: quantity,
                expiryDate: expiryDate
            ))
            onCreated(entry)
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
