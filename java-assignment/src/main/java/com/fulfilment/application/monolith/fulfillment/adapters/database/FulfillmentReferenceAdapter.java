package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentReferenceResolver;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

/** Resolves store, product and warehouse references against their own modules. */
@ApplicationScoped
public class FulfillmentReferenceAdapter implements FulfillmentReferenceResolver {

  private final ProductRepository productRepository;
  private final WarehouseStore warehouseStore;

  public FulfillmentReferenceAdapter(
      ProductRepository productRepository, WarehouseStore warehouseStore) {
    this.productRepository = productRepository;
    this.warehouseStore = warehouseStore;
  }

  @Override
  public boolean storeExists(Long storeId) {
    return Store.findById(storeId) != null;
  }

  @Override
  public boolean productExists(Long productId) {
    return productRepository.findById(productId) != null;
  }

  @Override
  public boolean activeWarehouseExists(String warehouseBusinessUnitCode) {
    return warehouseStore.findByBusinessUnitCode(warehouseBusinessUnitCode) != null;
  }
}
