package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

/**
 * Propagates Store changes to the legacy system strictly AFTER the database transaction committed
 * ({@link TransactionPhase#AFTER_SUCCESS}). If the transaction rolls back, this observer is never
 * invoked.
 */
@ApplicationScoped
public class LegacyStoreSyncObserver {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  void onStoreChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangedEvent event) {
    switch (event.type()) {
      case CREATED -> legacyStoreManagerGateway.createStoreOnLegacySystem(event.toStore());
      case UPDATED -> legacyStoreManagerGateway.updateStoreOnLegacySystem(event.toStore());
    }
  }
}
