package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.InMemoryWarehouseStore.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private ReplaceWarehouseUseCase useCase;
  private Warehouse current;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    useCase = new ReplaceWarehouseUseCase(store, new LocationGateway());
    current = warehouse("MWH.012", "AMSTERDAM-001", 50, 5);
    store.create(current);
  }

  @Test
  void archivesCurrentAndCreatesNewWithSameBusinessUnitCode() {
    var replacement = warehouse("MWH.012", "AMSTERDAM-001", 60, 5);

    useCase.replace(replacement);

    assertNotNull(current.archivedAt);
    assertNull(replacement.archivedAt);
    assertNotNull(replacement.createdAt);
    assertEquals(2, store.warehouses.size()); // history kept
    assertEquals(replacement, store.findByBusinessUnitCode("MWH.012"));
  }

  @Test
  void rejectsWhenStockDoesNotMatch() {
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.replace(warehouse("MWH.012", "AMSTERDAM-001", 60, 6)));
  }

  @Test
  void rejectsWhenCapacityCannotAccommodateOldStock() {
    // old stock is 5, new capacity 4 (stock matching is checked first, so keep stock = 5)
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.replace(warehouse("MWH.012", "AMSTERDAM-001", 4, 5)));
  }

  @Test
  void rejectsUnknownBusinessUnitCode() {
    assertThrows(
        WarehouseNotFoundException.class,
        () -> useCase.replace(warehouse("MWH.999", "AMSTERDAM-001", 60, 5)));
  }

  @Test
  void rejectsInvalidLocation() {
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.replace(warehouse("MWH.012", "NOWHERE-001", 60, 5)));
  }

  @Test
  void replacedWarehouseDoesNotCountAgainstLocationLimits() {
    // VETSBY-001 holds a single warehouse of max capacity 90: replacing it must still work
    var vetsby = warehouse("MWH.050", "VETSBY-001", 90, 10);
    store.create(vetsby);

    useCase.replace(warehouse("MWH.050", "VETSBY-001", 90, 10));

    assertNotNull(vetsby.archivedAt);
  }

  @Test
  void rejectsWhenNewLocationIsAlreadyFull() {
    store.create(warehouse("MWH.060", "HELMOND-001", 40, 1)); // HELMOND-001 allows only 1

    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.replace(warehouse("MWH.012", "HELMOND-001", 10, 5)));
  }
}
