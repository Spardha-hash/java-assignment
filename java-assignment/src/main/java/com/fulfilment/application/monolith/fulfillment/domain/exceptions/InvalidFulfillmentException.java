package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

/** The request is incomplete or breaks one of the fulfilment limits. */
public class InvalidFulfillmentException extends FulfillmentException {
  public InvalidFulfillmentException(String message) {
    super(message);
  }
}
