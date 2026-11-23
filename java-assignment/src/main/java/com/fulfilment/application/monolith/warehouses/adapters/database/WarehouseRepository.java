package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Database adapter of the {@link WarehouseStore} port. Archived warehouses stay in the table (they
 * are the history of a business unit) but are never returned by {@link #getAll()} or {@link
 * #findByBusinessUnitCode(String)}.
 */
@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    entity.archivedAt = warehouse.archivedAt;

    persist(entity);

    // hand the generated id back to the domain object
    warehouse.id = entity.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse entity = warehouse.id != null ? findById(warehouse.id) : null;
    if (entity == null) {
      entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
          .firstResult();
    }
    if (entity == null) {
      throw new WarehouseNotFoundException(
          "Warehouse '" + warehouse.businessUnitCode + "' does not exist.");
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;
    // entity is managed: changes are flushed when the surrounding transaction commits
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = warehouse.id != null ? findById(warehouse.id) : null;
    if (entity == null) {
      throw new WarehouseNotFoundException(
          "Warehouse '" + warehouse.businessUnitCode + "' does not exist.");
    }
    delete(entity);
  }

  /** Returns the ACTIVE warehouse of the given business unit, or null. */
  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity =
        find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  /** Looks a warehouse up by technical id (archived ones included), or null. */
  public Warehouse findWarehouseById(Long id) {
    DbWarehouse entity = findById(id);
    return entity == null ? null : entity.toWarehouse();
  }
}
