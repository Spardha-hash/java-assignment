package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  private static Warehouse warehouse(String code) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = "VETSBY-001";
    w.capacity = 10;
    w.stock = 1;
    return w;
  }

  @Test
  @Transactional
  public void createUpdateFindAndRemove() {
    Warehouse w = warehouse("REPO.1");
    repository.create(w);
    assertNotNull(w.id);
    assertNotNull(repository.findByBusinessUnitCode("REPO.1"));

    w.stock = 4;
    repository.update(w);
    assertEquals(4, repository.findWarehouseById(w.id).stock);

    // archived warehouses are no longer found by business unit code
    w.archivedAt = LocalDateTime.now();
    repository.update(w);
    assertNull(repository.findByBusinessUnitCode("REPO.1"));

    repository.remove(w);
    assertNull(repository.findWarehouseById(w.id));
  }

  @Test
  @Transactional
  public void updateFallsBackToBusinessUnitCodeWhenIdIsMissing() {
    Warehouse w = warehouse("REPO.2");
    repository.create(w);

    Warehouse detached = warehouse("REPO.2");
    detached.stock = 6;
    repository.update(detached);

    assertEquals(6, repository.findByBusinessUnitCode("REPO.2").stock);
  }

  @Test
  @Transactional
  public void updateAndRemoveOfUnknownWarehouseFail() {
    Warehouse ghost = warehouse("REPO.GHOST");
    assertThrows(WarehouseNotFoundException.class, () -> repository.update(ghost));
    assertThrows(WarehouseNotFoundException.class, () -> repository.remove(ghost));
  }
}
