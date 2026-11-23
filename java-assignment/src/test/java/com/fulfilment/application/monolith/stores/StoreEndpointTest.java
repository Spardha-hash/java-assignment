package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  @Test
  public void createUpdateAndPatchStore() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"TEST-STORE\",\"quantityProductsInStock\":4}")
            .post("store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"TEST-STORE-2\",\"quantityProductsInStock\":9}")
        .put("store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("TEST-STORE-2"))
        .body("quantityProductsInStock", equalTo(9));

    // PATCH only changes what was sent: quantity 0 means "not provided"
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"TEST-STORE-3\"}")
        .patch("store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("TEST-STORE-3"))
        .body("quantityProductsInStock", equalTo(9));
  }
}
