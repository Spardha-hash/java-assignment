package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * All business-rule validation for warehouses lives here, so the use cases only orchestrate and
 * every rule can be read (and tested) in one place.
 */
@ApplicationScoped
public class WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  /** Rules for creating a brand-new warehouse. */
  public void validateForCreation(Warehouse warehouse) {
    validateBasics(warehouse);

    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new InvalidWarehouseException(
          "A warehouse with business unit code '"
              + warehouse.businessUnitCode
              + "' already exists.");
    }

    validateLocationFit(warehouse, null);
  }

  /**
   * Rules for replacing the {@code current} active warehouse of a business unit by {@code
   * replacement}.
   */
  public void validateForReplacement(Warehouse replacement, Warehouse current) {
    // Stock Matching
    if (!replacement.stock.equals(current.stock)) {
      throw new InvalidWarehouseException(
          "Stock of the new warehouse ("
              + replacement.stock
              + ") must match the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    // Capacity Accommodation
    if (replacement.capacity < current.stock) {
      throw new InvalidWarehouseException(
          "Capacity of the new warehouse ("
              + replacement.capacity
              + ") cannot accommodate the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    // The warehouse being replaced no longer counts against the location limits
    validateLocationFit(replacement, current.businessUnitCode);
  }

  /** Only an existing, still active warehouse can be archived. */
  public void validateForArchive(Warehouse warehouse) {
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WarehouseNotFoundException("No active warehouse to archive.");
    }
  }

  /** Required fields and value ranges. */
  public void validateBasics(Warehouse warehouse) {
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
   * Location existence, number of warehouses allowed there, location capacity and stock vs capacity.
   *
   * @param ignoredBusinessUnitCode an active warehouse to leave out of the counts (the one being
   *     replaced), or null
   */
  private void validateLocationFit(Warehouse warehouse, String ignoredBusinessUnitCode) {
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
