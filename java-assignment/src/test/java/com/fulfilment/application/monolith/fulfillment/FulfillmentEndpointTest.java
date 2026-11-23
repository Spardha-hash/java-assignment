package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentEndpointTest {

  private static String assignment(long store, long product, String warehouse) {
    return "{\"storeId\":"
        + store
        + ",\"productId\":"
        + product
        + ",\"warehouseBusinessUnitCode\":\""
        + warehouse
        + "\"}";
  }

  private static int post(long store, long product, String warehouse) {
    return given()
        .contentType(ContentType.JSON)
        .body(assignment(store, product, warehouse))
        .post("fulfillment")
        .then()
        .extract()
        .statusCode();
  }

  private static void createWarehouse(String code, String location) {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"businessUnitCode\":\""
                + code
                + "\",\"location\":\""
                + location
                + "\",\"capacity\":10,\"stock\":1}")
        .post("warehouse")
        .then()
        .statusCode(201);
  }

  @Test
  public void enforcesStoreProductAndWarehouseLimits() {
    // Store 2 and products 2 and 3 exist in the seed data
    createWarehouse("F.W1", "HELMOND-001");
    createWarehouse("F.W2", "ZWOLLE-002");
    createWarehouse("F.W3", "EINDHOVEN-001");
    createWarehouse("F.W4", "AMSTERDAM-002");

    // happy path + duplicate
    org.junit.jupiter.api.Assertions.assertEquals(201, post(2, 2, "F.W1"));
    org.junit.jupiter.api.Assertions.assertEquals(409, post(2, 2, "F.W1"));

    // max 2 warehouses for the same product at the same store
    org.junit.jupiter.api.Assertions.assertEquals(201, post(2, 2, "F.W2"));
    org.junit.jupiter.api.Assertions.assertEquals(422, post(2, 2, "F.W3"));

    // max 3 warehouses per store: F.W1, F.W2 already used by store 2, add F.W3, then F.W4 is the 4th
    org.junit.jupiter.api.Assertions.assertEquals(201, post(2, 3, "F.W3"));
    org.junit.jupiter.api.Assertions.assertEquals(422, post(2, 3, "F.W4"));

    given().get("fulfillment/store/2").then().statusCode(200).body("$", hasSize(3));
  }

  @Test
  public void warehouseCanStoreAtMostFiveProductTypes() {
    createWarehouse("F.P", "VETSBY-001");

    // store 3 is only used by this test
    for (int i = 1; i <= 6; i++) {
      long productId =
          given()
              .contentType(ContentType.JSON)
              .body("{\"name\":\"FULFIL-PROD-" + i + "\",\"stock\":1}")
              .post("product")
              .then()
              .statusCode(201)
              .extract()
              .jsonPath()
              .getLong("id");

      int expected = i <= 5 ? 201 : 422;
      org.junit.jupiter.api.Assertions.assertEquals(expected, post(3, productId, "F.P"));
    }
  }

  @Test
  public void rejectsUnknownReferences() {
    org.junit.jupiter.api.Assertions.assertEquals(404, post(999, 2, "MWH.023"));
    org.junit.jupiter.api.Assertions.assertEquals(404, post(1, 999, "MWH.023"));
    org.junit.jupiter.api.Assertions.assertEquals(404, post(1, 2, "NOPE"));
  }
}
