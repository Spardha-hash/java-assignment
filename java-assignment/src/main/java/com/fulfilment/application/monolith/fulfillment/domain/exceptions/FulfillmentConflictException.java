package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

/** The same association already exists. */
public class FulfillmentConflictException extends FulfillmentException {
  public FulfillmentConflictException(String message) {
    super(message);
  }
}
