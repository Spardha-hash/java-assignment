package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * The OpenAPI contract says "POST /warehouse" answers 201 Created, but the generated resource
 * interface returns the bean directly, which JAX-RS answers with 200. This filter fixes the status
 * code for that one operation without changing the generated signature.
 */
@Provider
public class WarehouseCreatedStatusFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {
    String path = request.getUriInfo().getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    if ("POST".equals(request.getMethod()) && "warehouse".equals(path) && response.getStatus() == 200) {
      response.setStatus(201);
    }
  }
}
