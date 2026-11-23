package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;

/** Validation rules shared by the create and replace use cases. */
final class WarehouseValidator {

  private WarehouseValidator() {}

  static void validateBasics(Warehouse warehouse) {
    if (warehouse == null) {
      throw new InvalidWarehouseException("Warehouse data must be provided.");
    }
    if (isBlank(warehouse.businessUnitCode)) {
      throw new InvalidWarehouseException("Business unit code must be provided.");
    }
    if (isBlank(warehouse.location)) {
      throw new InvalidWarehouseException("Location must be provided.");
    }
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new InvalidWarehouseException("Capacity must be a positive number.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new InvalidWarehouseException("Stock must be zero or a positive number.");
    }
  }

  /**
   * Validates location existence, how many warehouses fit there and the location capacity.
   *
   * @param ignoredBusinessUnitCode an active warehouse to leave out of the counts (the one being
   *     replaced), or null
   */
  static void validateLocationFit(
      Warehouse warehouse,
      LocationResolver locationResolver,
      WarehouseStore warehouseStore,
      String ignoredBusinessUnitCode) {

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new InvalidWarehouseException("Location '" + warehouse.location + "' is not valid.");
    }

    List<Warehouse> activeAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> w.archivedAt == null)
            .filter(w -> location.identification.equalsIgnoreCase(w.location))
            .filter(w -> !w.businessUnitCode.equals(ignoredBusinessUnitCode))
            .toList();

    // Warehouse Creation Feasibility
    if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new InvalidWarehouseException(
          "Location '"
              + location.identification
              + "' already has the maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ").");
    }

    // Capacity validation: the sum of all warehouse capacities must fit the location
    int alreadyAllocated = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (alreadyAllocated + warehouse.capacity > location.maxCapacity) {
      throw new InvalidWarehouseException(
          "Capacity "
              + warehouse.capacity
              + " exceeds the remaining capacity of location '"
              + location.identification
              + "' ("
              + (location.maxCapacity - alreadyAllocated)
              + " of "
              + location.maxCapacity
              + " left).");
    }

    // Stock validation: the warehouse must be able to hold the informed stock
    if (warehouse.stock > warehouse.capacity) {
      throw new InvalidWarehouseException(
          "Stock (" + warehouse.stock + ") exceeds the warehouse capacity (" + warehouse.capacity
              + ").");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
