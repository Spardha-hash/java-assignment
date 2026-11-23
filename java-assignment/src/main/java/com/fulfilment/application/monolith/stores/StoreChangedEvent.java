package com.fulfilment.application.monolith.stores;

/**
 * Fired by {@link StoreResource} when a Store was created or updated. It is only <b>delivered</b>
 * to {@link LegacyStoreSyncObserver} once the surrounding database transaction has committed, so
 * the legacy system never receives data that was rolled back.
 *
 * <p>It carries an immutable snapshot of the data, not the managed entity.
 */
public record StoreChangedEvent(Type type, Long id, String name, int quantityProductsInStock) {

  public enum Type {
    CREATED,
    UPDATED
  }

  public static StoreChangedEvent of(Type type, Store store) {
    return new StoreChangedEvent(type, store.id, store.name, store.quantityProductsInStock);
  }

  /** Rebuilds a (transient) Store for the legacy gateway, which only needs name and quantity. */
  Store toStore() {
    Store store = new Store(name);
    store.quantityProductsInStock = quantityProductsInStock;
    return store;
  }
}
