import SwiftUI

struct ProductDetailView: View {
    let product: ProductResponse
    @State private var viewModel = ProductDetailViewModel()
    @State private var showingRegister = false

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.stockEntries.isEmpty {
                ProgressView("Loading entries...")
            } else if viewModel.stockEntries.isEmpty {
                ContentUnavailableView(
                    "No Stock Entries",
                    systemImage: "archivebox",
                    description: Text("Register the first batch for this product.")
                )
            } else {
                List {
                    ForEach(viewModel.stockEntries) { entry in
                        StockEntryRow(entry: entry)
                    }
                    .onDelete { offsets in
                        for index in offsets {
                            let id = viewModel.stockEntries[index].id
                            Task { await viewModel.deleteStockEntry(id: id) }
                        }
                    }
                }
            }
        }
        .navigationTitle(product.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button { showingRegister = true } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showingRegister) {
            RegisterStockEntryView(productId: product.id) { entry in
                viewModel.stockEntries.insert(entry, at: 0)
            }
        }
        .task { await viewModel.loadStockEntries(productId: product.id) }
        .alert("Error", isPresented: Binding(
            get: { viewModel.errorMessage != nil },
            set: { if !$0 { viewModel.errorMessage = nil } }
        )) {
            Button("Retry") { Task { await viewModel.loadStockEntries(productId: product.id) } }
            Button("OK", role: .cancel) {}
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
    }
}

private struct StockEntryRow: View {
    let entry: RegisterStockEntryResponse

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .none
        return f
    }()

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text("Qty: \(entry.quantity)")
                    .font(.body.monospacedDigit())
                Spacer()
                Label(
                    Self.dateFormatter.string(from: entry.expiryDate),
                    systemImage: "calendar.badge.clock"
                )
                .font(.caption)
                .foregroundStyle(expiresSoon ? .orange : .secondary)
            }
            Text("Received: \(Self.dateFormatter.string(from: entry.receivedDate))")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 2)
    }

    private var expiresSoon: Bool {
        let days = Calendar.current.dateComponents([.day], from: .now, to: entry.expiryDate).day ?? 0
        return days <= 30
    }
}
