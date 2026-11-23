package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;

/** JSON shape of a fulfilment association. */
public class FulfillmentDto {

  public Long id;
  public Long storeId;
  public Long productId;
  public String warehouseBusinessUnitCode;

  public FulfillmentDto() {}

  static FulfillmentDto from(FulfillmentAssignment assignment) {
    FulfillmentDto dto = new FulfillmentDto();
    dto.id = assignment.id;
    dto.storeId = assignment.storeId;
    dto.productId = assignment.productId;
    dto.warehouseBusinessUnitCode = assignment.warehouseBusinessUnitCode;
    return dto;
  }

  FulfillmentAssignment toDomain() {
    FulfillmentAssignment assignment =
        new FulfillmentAssignment(storeId, productId, warehouseBusinessUnitCode);
    assignment.id = id;
    return assignment;
  }
}
