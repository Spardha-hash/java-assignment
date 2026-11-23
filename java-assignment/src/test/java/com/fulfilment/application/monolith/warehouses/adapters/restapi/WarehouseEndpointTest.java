package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * HTTP-level tests. Each test creates its own warehouses (in locations the seed data leaves empty)
 * so they do not depend on each other or on test execution order.
 */
@QuarkusTest
public class WarehouseEndpointTest {

  private static String body(String code, String location, int capacity, int stock) {
    return "{\"businessUnitCode\":\""
        + code
        + "\",\"location\":\""
        + location
        + "\",\"capacity\":"
        + capacity
        + ",\"stock\":"
        + stock
        + "}";
  }

  private static io.restassured.specification.RequestSpecification json() {
    return given().contentType(ContentType.JSON);
  }

  @Test
  public void createGetAndArchive() {
    String id =
        json()
            .body(body("T.CREATE", "ZWOLLE-002", 20, 5))
            .when()
            .post("warehouse")
            .then()
            .statusCode(201)
            .body("businessUnitCode", equalTo("T.CREATE"))
            .body("id", notNullValue())
            .extract()
            .path("id");

    given().when().get("warehouse/" + id).then().statusCode(200).body("location", equalTo("ZWOLLE-002"));

    given().when().delete("warehouse/" + id).then().statusCode(204);

    given().when().get("warehouse/" + id).then().statusCode(404);
    given().when().get("warehouse").then().statusCode(200).body(not(containsString("T.CREATE")));
  }

  @Test
  public void createRejectsInvalidRequests() {
    // unknown location
    json().body(body("T.BAD1", "NOWHERE-1", 20, 5)).post("warehouse").then().statusCode(400);
    // capacity above location maximum (VETSBY-001 max 90)
    json().body(body("T.BAD2", "VETSBY-001", 91, 5)).post("warehouse").then().statusCode(400);
    // stock above capacity
    json().body(body("T.BAD3", "AMSTERDAM-002", 10, 11)).post("warehouse").then().statusCode(400);
    // duplicate business unit code (seed data)
    json().body(body("MWH.012", "AMSTERDAM-002", 10, 1)).post("warehouse").then().statusCode(400);
    // location already full (TILBURG-001 allows only 1 and the seed data uses it)
    json().body(body("T.BAD4", "TILBURG-001", 10, 1)).post("warehouse").then().statusCode(400);
  }

  @Test
  public void replaceWarehouse() {
    json().body(body("T.REPL", "AMSTERDAM-002", 30, 8)).post("warehouse").then().statusCode(201);

    // stock mismatch
    json().body(body("ignored", "AMSTERDAM-002", 40, 9))
        .post("warehouse/T.REPL/replacement")
        .then()
        .statusCode(400);

    // capacity cannot hold the old stock (8)
    json().body(body("ignored", "AMSTERDAM-002", 7, 8))
        .post("warehouse/T.REPL/replacement")
        .then()
        .statusCode(400);

    // valid replacement keeps the business unit code
    json().body(body("ignored", "AMSTERDAM-002", 40, 8))
        .post("warehouse/T.REPL/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("T.REPL"))
        .body("capacity", equalTo(40));

    // unknown business unit
    json().body(body("x", "AMSTERDAM-002", 40, 8))
        .post("warehouse/DOES.NOT.EXIST/replacement")
        .then()
        .statusCode(404);
  }

  @Test
  public void archiveUnknownWarehouseReturns404() {
    given().when().delete("warehouse/987654").then().statusCode(404);
    given().when().delete("warehouse/not-a-number").then().statusCode(404);
  }
}
