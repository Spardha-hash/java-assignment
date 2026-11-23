package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;

/** Tiny in-memory fake of the {@link WarehouseStore} port for pure unit tests. */
class InMemoryWarehouseStore implements WarehouseStore {

  final List<Warehouse> warehouses = new ArrayList<>();

  static Warehouse warehouse(String code, String location, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  /** Active warehouses only, like the real adapter. */
  @Override
  public List<Warehouse> getAll() {
    return warehouses.stream().filter(w -> w.archivedAt == null).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    warehouses.add(warehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    // objects are shared in memory, nothing to do
  }

  @Override
  public void remove(Warehouse warehouse) {
    warehouses.remove(warehouse);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    return getAll().stream().filter(w -> w.businessUnitCode.equals(buCode)).findFirst().orElse(null);
  }
}
