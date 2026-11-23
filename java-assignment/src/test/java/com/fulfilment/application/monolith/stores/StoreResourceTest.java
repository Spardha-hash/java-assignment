package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/** Covers the remaining Store operations. Never modifies the seeded stores. */
@QuarkusTest
public class StoreResourceTest {

  @Test
  public void listAndGetSingle() {
    given().get("store").then().statusCode(200).body("size()", greaterThanOrEqualTo(3));
    given().get("store/1").then().statusCode(200).body("name", equalTo("TONSTAD"));
    given().get("store/987654").then().statusCode(404);
  }

  @Test
  public void createThenDelete() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"STORE-DEL\",\"quantityProductsInStock\":1}")
            .post("store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given().delete("store/" + id).then().statusCode(204);
    given().get("store/" + id).then().statusCode(404);
    given().delete("store/" + id).then().statusCode(404);
  }

  @Test
  public void rejectsInvalidRequests() {
    // id must not be set on create
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\":9000,\"name\":\"STORE-BAD\"}")
        .post("store")
        .then()
        .statusCode(422);

    // name is required for PUT and PATCH
    given().contentType(ContentType.JSON).body("{\"quantityProductsInStock\":1}")
        .put("store/1").then().statusCode(422);
    given().contentType(ContentType.JSON).body("{\"quantityProductsInStock\":1}")
        .patch("store/1").then().statusCode(422);

    // unknown store
    given().contentType(ContentType.JSON).body("{\"name\":\"X\"}")
        .put("store/987654").then().statusCode(404);
    given().contentType(ContentType.JSON).body("{\"name\":\"X\"}")
        .patch("store/987654").then().statusCode(404);
  }
}
