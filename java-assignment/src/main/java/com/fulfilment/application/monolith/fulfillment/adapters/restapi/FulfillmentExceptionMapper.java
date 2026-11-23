package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentConflictException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Not found -> 404, duplicate -> 409, any other rule violation -> 422. */
@Provider
public class FulfillmentExceptionMapper implements ExceptionMapper<FulfillmentException> {

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(FulfillmentException exception) {
    int code = 422;
    if (exception instanceof FulfillmentNotFoundException) {
      code = 404;
    } else if (exception instanceof FulfillmentConflictException) {
      code = 409;
    }

    ObjectNode body = objectMapper.createObjectNode();
    body.put("exceptionType", exception.getClass().getName());
    body.put("code", code);
    body.put("error", exception.getMessage());

    return Response.status(code).type(MediaType.APPLICATION_JSON).entity(body).build();
  }
}
