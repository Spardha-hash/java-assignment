package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentResourceTest {

  @Test
  public void createListAndDelete() {
    // seed data: store 1, product 3, active warehouse MWH.023
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"storeId\":1,\"productId\":3,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
            .post("fulfillment")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given().get("fulfillment/store/1").then().statusCode(200).body("$", hasSize(1));

    given().delete("fulfillment/" + id).then().statusCode(204);
    given().delete("fulfillment/" + id).then().statusCode(404);
    given().get("fulfillment/store/1").then().statusCode(200).body("$", hasSize(0));
  }

  @Test
  public void rejectsIncompleteOrInvalidRequests() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"storeId\":1}")
        .post("fulfillment")
        .then()
        .statusCode(422);

    given()
        .contentType(ContentType.JSON)
        .body("{\"id\":77,\"storeId\":1,\"productId\":3,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
        .post("fulfillment")
        .then()
        .statusCode(422);
  }
}
