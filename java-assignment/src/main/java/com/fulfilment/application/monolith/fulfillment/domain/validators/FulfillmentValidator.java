package com.fulfilment.application.monolith.fulfillment.domain.validators;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentConflictException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.InvalidFulfillmentException;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentReferenceResolver;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * All fulfilment business rules in one place:
 *
 * <ol>
 *   <li>a Product can be fulfilled by at most 2 different Warehouses per Store
 *   <li>a Store can be fulfilled by at most 3 different Warehouses
 *   <li>a Warehouse can store at most 5 different Products
 * </ol>
 */
@ApplicationScoped
public class FulfillmentValidator {

  public static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  public static final int MAX_WAREHOUSES_PER_STORE = 3;
  public static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfillmentStore store;
  private final FulfillmentReferenceResolver references;

  public FulfillmentValidator(FulfillmentStore store, FulfillmentReferenceResolver references) {
    this.store = store;
    this.references = references;
  }

  /** Full validation of a new association. */
  public void validateForAssignment(FulfillmentAssignment assignment) {
    validateRequest(assignment);
    validateReferences(assignment);
    validateNotDuplicate(assignment);
    validateLimits(assignment);
  }

  /** Required fields. */
  public void validateRequest(FulfillmentAssignment assignment) {
    if (assignment == null
        || assignment.storeId == null
        || assignment.productId == null
        || assignment.warehouseBusinessUnitCode == null
        || assignment.warehouseBusinessUnitCode.isBlank()) {
      throw new InvalidFulfillmentException(
          "storeId, productId and warehouseBusinessUnitCode must be set.");
    }
    if (assignment.id != null) {
      throw new InvalidFulfillmentException("Id was invalidly set on request.");
    }
  }

  /** The store, the product and an ACTIVE warehouse must exist. */
  public void validateReferences(FulfillmentAssignment assignment) {
    if (!references.storeExists(assignment.storeId)) {
      throw new FulfillmentNotFoundException("Store " + assignment.storeId + " does not exist.");
    }
    if (!references.productExists(assignment.productId)) {
      throw new FulfillmentNotFoundException(
          "Product " + assignment.productId + " does not exist.");
    }
    if (!references.activeWarehouseExists(assignment.warehouseBusinessUnitCode)) {
      throw new FulfillmentNotFoundException(
          "No active warehouse with business unit code "
              + assignment.warehouseBusinessUnitCode
              + ".");
    }
  }

  public void validateNotDuplicate(FulfillmentAssignment assignment) {
    if (store.exists(
        assignment.storeId, assignment.productId, assignment.warehouseBusinessUnitCode)) {
      throw new FulfillmentConflictException("This fulfillment association already exists.");
    }
  }

  /** The three capacity limits. */
  public void validateLimits(FulfillmentAssignment assignment) {
    String warehouse = assignment.warehouseBusinessUnitCode;

    // 1. max 2 warehouses for the same product at the same store
    if (store.findByStoreAndProduct(assignment.storeId, assignment.productId).size()
        >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new InvalidFulfillmentException(
          "A product can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store.");
    }

    // 2. max 3 warehouses per store (a warehouse already serving the store is not new)
    List<String> warehousesOfStore =
        store.findByStore(assignment.storeId).stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .distinct()
            .toList();
    if (!warehousesOfStore.contains(warehouse)
        && warehousesOfStore.size() >= MAX_WAREHOUSES_PER_STORE) {
      throw new InvalidFulfillmentException(
          "A store can be fulfilled by at most " + MAX_WAREHOUSES_PER_STORE + " warehouses.");
    }

    // 3. max 5 products per warehouse (a product already stored there is not new)
    List<Long> productsOfWarehouse =
        store.findByWarehouse(warehouse).stream().map(a -> a.productId).distinct().toList();
    if (!productsOfWarehouse.contains(assignment.productId)
        && productsOfWarehouse.size() >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new InvalidFulfillmentException(
          "A warehouse can store at most " + MAX_PRODUCTS_PER_WAREHOUSE + " types of products.");
    }
  }
}
