package com.fulfilment.application.monolith.warehouses.domain.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseValidatorTest {

  private final List<Warehouse> data = new ArrayList<>();
  private WarehouseValidator validator;

  private static Warehouse warehouse(String code, String location, Integer capacity, Integer stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = code;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @BeforeEach
  void setUp() {
    WarehouseStore store =
        new WarehouseStore() {
          @Override
          public List<Warehouse> getAll() {
            return data.stream().filter(w -> w.archivedAt == null).toList();
          }

          @Override
          public void create(Warehouse warehouse) {
            data.add(warehouse);
          }

          @Override
          public void update(Warehouse warehouse) {}

          @Override
          public void remove(Warehouse warehouse) {
            data.remove(warehouse);
          }

          @Override
          public Warehouse findByBusinessUnitCode(String buCode) {
            return getAll().stream()
                .filter(w -> w.businessUnitCode.equals(buCode))
                .findFirst()
                .orElse(null);
          }
        };
    validator = new WarehouseValidator(store, new LocationGateway());
  }

  @Test
  void acceptsValidNewWarehouse() {
    assertDoesNotThrow(
        () -> validator.validateForCreation(warehouse("V.1", "EINDHOVEN-001", 30, 10)));
  }

  @Test
  void rejectsNullAndIncompleteWarehouses() {
    assertThrows(InvalidWarehouseException.class, () -> validator.validateBasics(null));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateBasics(warehouse(" ", "EINDHOVEN-001", 30, 10)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateBasics(warehouse("V.1", null, 30, 10)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateBasics(warehouse("V.1", "EINDHOVEN-001", null, 10)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateBasics(warehouse("V.1", "EINDHOVEN-001", 30, null)));
  }

  @Test
  void rejectsDuplicateCodeUnknownLocationAndOverCapacity() {
    data.add(warehouse("V.1", "EINDHOVEN-001", 30, 10));

    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateForCreation(warehouse("V.1", "ZWOLLE-002", 10, 1)));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateForCreation(warehouse("V.2", "NOWHERE-1", 10, 1)));
    // 30 already allocated at EINDHOVEN-001 (max 70): 41 does not fit
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateForCreation(warehouse("V.2", "EINDHOVEN-001", 41, 1)));
  }

  @Test
  void replacementRulesAreChecked() {
    Warehouse current = warehouse("V.1", "AMSTERDAM-001", 50, 5);
    data.add(current);

    assertDoesNotThrow(
        () -> validator.validateForReplacement(warehouse("V.1", "AMSTERDAM-001", 60, 5), current));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateForReplacement(warehouse("V.1", "AMSTERDAM-001", 60, 6), current));
    assertThrows(
        InvalidWarehouseException.class,
        () -> validator.validateForReplacement(warehouse("V.1", "AMSTERDAM-001", 4, 5), current));
  }

  @Test
  void onlyActiveWarehousesCanBeArchived() {
    assertThrows(WarehouseNotFoundException.class, () -> validator.validateForArchive(null));

    Warehouse archived = warehouse("V.1", "AMSTERDAM-001", 50, 5);
    archived.archivedAt = LocalDateTime.now();
    assertThrows(WarehouseNotFoundException.class, () -> validator.validateForArchive(archived));

    assertDoesNotThrow(
        () -> validator.validateForArchive(warehouse("V.2", "AMSTERDAM-001", 50, 5)));
  }
}
