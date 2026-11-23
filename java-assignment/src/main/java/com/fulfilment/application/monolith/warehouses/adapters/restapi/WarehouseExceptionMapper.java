package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Maps domain failures to HTTP: not found -> 404, any other business rule violation -> 400. */
@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseException> {

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(WarehouseException exception) {
    int code = exception instanceof WarehouseNotFoundException ? 404 : 400;

    ObjectNode body = objectMapper.createObjectNode();
    body.put("exceptionType", exception.getClass().getName());
    body.put("code", code);
    body.put("error", exception.getMessage());

    return Response.status(code).type(MediaType.APPLICATION_JSON).entity(body).build();
  }
}
