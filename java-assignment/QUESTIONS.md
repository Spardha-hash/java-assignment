# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, but incrementally, and mainly for consistency. Today three styles live side by side:
Store uses the Active Record style (PanacheEntity with static calls such as Store.findById),
Product uses a PanacheRepository injected into the resource, and Warehouse uses a hexagonal
setup (domain model + WarehouseStore port + WarehouseRepository adapter + a separate DbWarehouse
JPA entity).

What I would do:
- Make the Repository pattern the single convention. Static Active Record calls are hard to
  mock or replace in unit tests and they tie the resource directly to the database. A
  repository can be faked, as I did with the in-memory WarehouseStore in the use case tests.
- Keep the full ports-and-adapters split (domain model separate from the JPA entity) for the
  Warehouse, because it carries real business rules (capacity, location limits, replacement)
  that should be testable without a database. Store and Product are plain CRUD, so a
  repository plus a thin service is enough there. I would not copy the Warehouse ceremony
  everywhere.
- Move @Transactional and business logic out of the REST resources into services/use cases.
  Resources should only translate HTTP <-> domain. This also makes the Store fix natural:
  the legacy sync is an after-commit event, not something the HTTP layer orchestrates.
- Stop returning JPA entities directly from the API (Store, Product). A DTO layer avoids
  leaking persistence details and lets the schema evolve independently of the contract.
- Add real constraints/migrations (Flyway) instead of drop-and-create, e.g. a partial unique
  index on business unit code for active warehouses, which also closes a race condition in
  the create check.

I would do this opportunistically, when touching each area, rather than as a big-bang
refactor: it is low-risk, easy to review and keeps the tests green throughout.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Contract-first (the OpenAPI yaml + generated interfaces, used for Warehouse):
Pros
- The contract is explicit, reviewable and versionable before any code exists; frontend,
  other teams and the client can work in parallel (mocks, SDK generation).
- The server cannot silently drift from the documented contract, and breaking changes can be
  detected in CI (e.g. openapi-diff).
- Consistent status codes, error shapes and naming across endpoints; docs come for free.
Cons
- Extra build step and tooling; generated code can be limiting or awkward (for example a
  generated method returning the bean makes it harder to control the status code or headers,
  and generated names like createANewWarehouseUnit are ugly).
- Slower to iterate on while an API is still being discovered; two artefacts to keep in
  sync mentally (yaml and implementation).

Code-first (direct JAX-RS resources, used for Product and Store):
Pros
- Fastest to write and refactor; the IDE and compiler help; flexible response handling.
Cons
- Easy to change the API by accident (renaming a field changes the contract); the spec, if
  any, is a by-product. The entities are exposed directly, which couples the API to the DB.
- Inconsistency between endpoints (error formats, validation) as each developer decides.

My choice: contract-first for any API that other teams or systems consume, which is the case
for these three resources, with the same conventions for all of them. I would bring Product
and Store under the same approach as they stabilise. For a small internal or prototype API,
code-first plus a generated spec (quarkus-smallrye-openapi) and a CI check that the spec did
not change unexpectedly is an acceptable trade-off. What matters most is that there is one
convention across the code base.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritise by risk and by cost of feedback, following the test pyramid:

1. Unit tests for the domain/use cases (largest share, fastest). The business rules live
   here: unique business unit code, location validity, max warehouses per location, capacity
   and stock rules, replacement rules. They run in milliseconds with in-memory fakes, so I
   cover every rule and boundary (equal to the limit, one above, null/blank input).
2. A smaller set of @QuarkusTest integration tests per endpoint with RestAssured and the
   real database (Dev Services PostgreSQL). They check what unit tests cannot: JSON mapping,
   HTTP status codes and error mapping, transactions, queries (archived warehouses excluded)
   and the Store behaviour that the legacy system is only called after commit. For that one I
   would add a mock of the legacy gateway and assert it is NOT called when the transaction
   rolls back.
3. Contract tests against the OpenAPI spec so the implementation cannot drift from it.
4. Very few end-to-end/@QuarkusIntegrationTest tests against the packaged app: happy-path
   smoke tests only, because they are slow and brittle.

To keep coverage effective over time:
- Run everything in CI on every pull request; fast unit tests first, integration tests after.
- Use JaCoCo with a threshold on new/changed code (and a higher one on the domain package)
  instead of chasing a global percentage; consider mutation testing (PIT) on the domain
  package to check that the tests would actually catch a broken rule.
- Make tests independent (each creates its own data, no reliance on execution order or on the
  seed data being untouched), use test data builders and keep them readable.
- Every bug fix starts with a failing test, so regressions are covered by default.
- Treat flaky tests as bugs, and review test quality in code review like production code.
- Add concurrency tests for the capacity/limit checks later, since two parallel requests can
  currently both pass the validation.
```