package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/** Covers the remaining Product operations. Works only on products it creates itself. */
@QuarkusTest
public class ProductResourceTest {

  @Test
  public void createGetUpdateAndDelete() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"PR-TEST-1\",\"stock\":3}")
            .post("product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given().get("product/" + id).then().statusCode(200).body("name", equalTo("PR-TEST-1"));

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"PR-TEST-2\",\"description\":\"updated\",\"price\":9.5,\"stock\":7}")
        .put("product/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("PR-TEST-2"))
        .body("description", equalTo("updated"))
        .body("stock", equalTo(7));

    given().delete("product/" + id).then().statusCode(204);
    given().get("product/" + id).then().statusCode(404);
    given().delete("product/" + id).then().statusCode(404);
  }

  @Test
  public void rejectsInvalidRequests() {
    // id must not be set on create
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\":5000,\"name\":\"PR-TEST-3\"}")
        .post("product")
        .then()
        .statusCode(422);

    // name is required on update
    given()
        .contentType(ContentType.JSON)
        .body("{\"stock\":1}")
        .put("product/2")
        .then()
        .statusCode(422);

    // unknown product
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"whatever\"}")
        .put("product/987654")
        .then()
        .statusCode(404);
    given().get("product/987654").then().statusCode(404);
  }
}
