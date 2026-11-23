package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.ports.AssignFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.ListFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.RemoveFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

/** REST adapter for fulfilment associations (bonus task). Contains no business rules. */
@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  @Inject AssignFulfillmentOperation assignOperation;
  @Inject RemoveFulfillmentOperation removeOperation;
  @Inject ListFulfillmentOperation listOperation;

  @GET
  @Path("store/{storeId}")
  public List<FulfillmentDto> listByStore(Long storeId) {
    return listOperation.listByStore(storeId).stream().map(FulfillmentDto::from).toList();
  }

  @POST
  @Transactional
  public Response create(FulfillmentDto request) {
    var created = assignOperation.assign(request == null ? null : request.toDomain());
    return Response.status(201).entity(FulfillmentDto.from(created)).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    removeOperation.remove(id);
    return Response.status(204).build();
  }
}
