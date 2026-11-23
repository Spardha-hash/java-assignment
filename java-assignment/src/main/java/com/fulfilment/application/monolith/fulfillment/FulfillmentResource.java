package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BONUS: associates Warehouses as fulfilment units of Products for Stores.
 *
 * <ol>
 *   <li>a Product can be fulfilled by at most 2 different Warehouses per Store
 *   <li>a Store can be fulfilled by at most 3 different Warehouses
 *   <li>a Warehouse can store at most 5 different Products
 * </ol>
 */
@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject FulfillmentAssignmentRepository repository;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  @GET
  @Path("store/{storeId}")
  public List<FulfillmentAssignment> listByStore(Long storeId) {
    return repository.list("storeId", storeId);
  }

  @POST
  @Transactional
  public Response create(FulfillmentAssignment request) {
    if (request == null
        || request.storeId == null
        || request.productId == null
        || request.warehouseBusinessUnitCode == null
        || request.warehouseBusinessUnitCode.isBlank()) {
      throw new WebApplicationException(
          "storeId, productId and warehouseBusinessUnitCode must be set.", 422);
    }
    if (request.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    if (Store.findById(request.storeId) == null) {
      throw new WebApplicationException("Store " + request.storeId + " does not exist.", 404);
    }
    if (productRepository.findById(request.productId) == null) {
      throw new WebApplicationException("Product " + request.productId + " does not exist.", 404);
    }
    if (warehouseRepository.findByBusinessUnitCode(request.warehouseBusinessUnitCode) == null) {
      throw new WebApplicationException(
          "No active warehouse with business unit code "
              + request.warehouseBusinessUnitCode
              + ".",
          404);
    }

    String warehouse = request.warehouseBusinessUnitCode;

    if (repository.count(
            "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            request.storeId,
            request.productId,
            warehouse)
        > 0) {
      throw new WebApplicationException("This fulfillment association already exists.", 409);
    }

    // 1. max 2 different warehouses per product per store
    long warehousesForProductAtStore =
        repository.count("storeId = ?1 and productId = ?2", request.storeId, request.productId);
    if (warehousesForProductAtStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException(
          "A product can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store.",
          422);
    }

    // 2. max 3 different warehouses per store (a warehouse already serving the store is not new)
    List<String> warehousesOfStore =
        repository.list("storeId", request.storeId).stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .distinct()
            .collect(Collectors.toList());
    if (!warehousesOfStore.contains(warehouse)
        && warehousesOfStore.size() >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException(
          "A store can be fulfilled by at most " + MAX_WAREHOUSES_PER_STORE + " warehouses.", 422);
    }

    // 3. max 5 different products per warehouse (a product already stored there is not new)
    List<Long> productsOfWarehouse =
        repository.list("warehouseBusinessUnitCode", warehouse).stream()
            .map(a -> a.productId)
            .distinct()
            .collect(Collectors.toList());
    if (!productsOfWarehouse.contains(request.productId)
        && productsOfWarehouse.size() >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new WebApplicationException(
          "A warehouse can store at most " + MAX_PRODUCTS_PER_WAREHOUSE + " types of products.",
          422);
    }

    repository.persist(request);
    return Response.status(201).entity(request).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    FulfillmentAssignment entity = repository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Fulfillment " + id + " does not exist.", 404);
    }
    repository.delete(entity);
    return Response.status(204).build();
  }
}
