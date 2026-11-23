package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.InMemoryWarehouseStore.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    useCase = new CreateWarehouseUseCase(store, new LocationGateway());
  }

  @Test
  void createsWarehouseWhenAllRulesPass() {
    var w = warehouse("MWH.100", "EINDHOVEN-001", 30, 10);

    useCase.create(w);

    assertEquals(1, store.warehouses.size());
    assertNotNull(w.createdAt);
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    store.create(warehouse("MWH.100", "EINDHOVEN-001", 30, 10));

    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "ZWOLLE-002", 20, 5)));
  }

  @Test
  void rejectsUnknownLocation() {
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "NOWHERE-001", 20, 5)));
  }

  @Test
  void rejectsWhenMaxNumberOfWarehousesAtLocationIsReached() {
    // VETSBY-001 allows only 1 warehouse
    store.create(warehouse("MWH.100", "VETSBY-001", 30, 10));

    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.101", "VETSBY-001", 20, 5)));
  }

  @Test
  void rejectsCapacityAboveLocationMaximum() {
    // HELMOND-001 max capacity is 45
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "HELMOND-001", 46, 5)));
  }

  @Test
  void rejectsWhenSumOfCapacitiesExceedsLocationMaximum() {
    // EINDHOVEN-001: max 2 warehouses, max capacity 70
    store.create(warehouse("MWH.100", "EINDHOVEN-001", 50, 10));

    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.101", "EINDHOVEN-001", 30, 5)));
  }

  @Test
  void rejectsStockAboveCapacity() {
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "EINDHOVEN-001", 20, 21)));
  }

  @Test
  void rejectsMissingOrInvalidFields() {
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse(null, "EINDHOVEN-001", 20, 5)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "EINDHOVEN-001", 0, 0)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "EINDHOVEN-001", 20, -1)));
  }
}
