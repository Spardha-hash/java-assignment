package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.InMemoryWarehouseStore.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  void setsArchivedAtAndRemovesItFromActiveWarehouses() {
    var store = new InMemoryWarehouseStore();
    var w = warehouse("MWH.001", "ZWOLLE-001", 40, 10);
    store.create(w);

    new ArchiveWarehouseUseCase(store, new WarehouseValidator(store, new LocationGateway())).archive(w);

    assertNotNull(w.archivedAt);
    assertTrue(store.getAll().isEmpty());
    assertEquals(1, store.warehouses.size()); // kept as history
  }

  @Test
  void rejectsAlreadyArchivedWarehouse() {
    var store = new InMemoryWarehouseStore();
    var w = warehouse("MWH.001", "ZWOLLE-001", 40, 10);
    w.archivedAt = LocalDateTime.now();

    assertThrows(WarehouseNotFoundException.class, () -> new ArchiveWarehouseUseCase(store, new WarehouseValidator(store, new LocationGateway())).archive(w));
  }
}
