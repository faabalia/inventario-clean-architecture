import SwiftUI

struct ProductListView: View {
    @State private var viewModel = ProductListViewModel()
    @State private var showingAddProduct = false

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.products.isEmpty {
                    ProgressView("Loading products...")
                } else if viewModel.products.isEmpty {
                    ContentUnavailableView(
                        "No Products",
                        systemImage: "shippingbox",
                        description: Text("Add your first product to get started.")
                    )
                } else {
                    productList
                }
            }
            .navigationTitle("Products")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showingAddProduct = true } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddProduct) {
                AddProductView { product in
                    viewModel.products.insert(product, at: 0)
                }
            }
            .task { await viewModel.loadProducts() }
            .alert("Error", isPresented: Binding(
                get: { viewModel.errorMessage != nil },
                set: { if !$0 { viewModel.errorMessage = nil } }
            )) {
                Button("Retry") { Task { await viewModel.loadProducts() } }
                Button("OK", role: .cancel) {}
            } message: {
                Text(viewModel.errorMessage ?? "")
            }
        }
    }

    private var productList: some View {
        List {
            ForEach(viewModel.products) { product in
                NavigationLink {
                    ProductDetailView(product: product)
                } label: {
                    ProductRow(product: product)
                }
                .onAppear {
                    if product.id == viewModel.products.last?.id {
                        Task { await viewModel.loadNextPage() }
                    }
                }
            }
            if viewModel.isLoading {
                HStack { Spacer(); ProgressView(); Spacer() }
                    .listRowSeparator(.hidden)
            }
        }
    }
}

private struct ProductRow: View {
    let product: ProductResponse

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(product.name)
                .font(.body)
            Text(product.sku)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 2)
    }
}
