import SwiftUI

struct AddProductView: View {
    var onCreated: (ProductResponse) -> Void

    @State private var sku = ""
    @State private var name = ""
    @State private var description = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss

    private let service = ProductService()

    private var canSave: Bool {
        !sku.trimmingCharacters(in: .whitespaces).isEmpty &&
        !name.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Product Info") {
                    TextField("SKU (e.g. LCHE-001)", text: $sku)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.characters)
                    TextField("Name", text: $name)
                    TextField("Description (optional)", text: $description)
                }
            }
            .navigationTitle("New Product")
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
                            .disabled(!canSave)
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
            let desc = description.trimmingCharacters(in: .whitespaces)
            let product = try await service.createProduct(CreateProductRequest(
                sku: sku.trimmingCharacters(in: .whitespaces),
                name: name.trimmingCharacters(in: .whitespaces),
                description: desc.isEmpty ? nil : desc
            ))
            onCreated(product)
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
