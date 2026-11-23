package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

/**
 * Replaces the active warehouse of a business unit: the current one is archived and the new one is
 * created re-using the same business unit code, which keeps the history of that business unit.
 *
 * <p>Callers must run this inside one transaction so archive + create are atomic.
 */
@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    WarehouseValidator.validateBasics(newWarehouse);

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WarehouseNotFoundException(
          "No active warehouse found for business unit code '"
              + newWarehouse.businessUnitCode
              + "'.");
    }

    // Stock Matching
    if (!newWarehouse.stock.equals(current.stock)) {
      throw new InvalidWarehouseException(
          "Stock of the new warehouse ("
              + newWarehouse.stock
              + ") must match the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    // Capacity Accommodation
    if (newWarehouse.capacity < current.stock) {
      throw new InvalidWarehouseException(
          "Capacity of the new warehouse ("
              + newWarehouse.capacity
              + ") cannot accommodate the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    // Location / max warehouses / location capacity (the replaced warehouse no longer counts)
    WarehouseValidator.validateLocationFit(
        newWarehouse, locationResolver, warehouseStore, current.businessUnitCode);

    LocalDateTime now = LocalDateTime.now();

    current.archivedAt = now;
    warehouseStore.update(current);

    newWarehouse.createdAt = now;
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
