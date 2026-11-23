# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
- **Business questions.** What are the cost types and who owns each (labour, inventory carrying, transport, overhead)? What decisions will the numbers drive (pricing, store replenishment, closing a warehouse)? That tells us the level of accuracy needed and how often.
- **Allocation model.** Direct costs (a warehouse's staff, rent) are easy; shared costs (transport between warehouses, overhead, a shared team) need an agreed driver: volume, pallets, orders, headcount, or time. Which driver, who agrees on it, and can it be changed without rewriting history?
- **Granularity.** Per Business Unit Code, per Warehouse, per Store, per Product? The data model in this system (warehouse, store, product and their fulfilment links) suggests costs should be tagged with those dimensions at the source, not reconstructed afterwards.
- **Data quality and sources.** Costs come from ERP/finance, WMS, payroll, carrier invoices. Late or corrected invoices, different currencies and cut-off dates are the usual sources of mismatches, so the system needs idempotent ingestion, reconciliation against the ledger and an audit trail.
- **Time.** Costs belong to a period; accruals and corrections must be possible without breaking reports that were already published (append-only entries, versioned snapshots).
- **Experience to relate to.** In previous work, the most common problem was not the arithmetic but disagreement on definitions between teams. I would start with a small set of agreed cost categories and one or two reports that finance trusts, then widen.
- **Success measure.** Cost per order/pallet per warehouse reconciles with finance to within an agreed tolerance.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
- **Baseline first.** What is the current cost per order/unit by warehouse and what is the service level (delivery time, accuracy, stock-outs)? Without a baseline we cannot prove savings or notice quality dropping.
- **Where the money is.** Rank cost drivers (typically transport and labour, then inventory and space) and look for the largest, most controllable ones. A simple impact vs effort vs risk matrix prioritises candidates.
- **Candidate strategies.** Consolidate shipments and optimise routes; better location choice and use of capacity (this model already has per-location capacity and warehouse limits, so under-used capacity is visible); balance stock between warehouses to reduce emergency transfers; labour planning against demand; renegotiate carrier contracts; reduce excess inventory.
- **Constraints.** Service-level commitments, contractual obligations, location limits (max warehouses and capacity per location) and the 'each store fulfilled by max 3 warehouses' style rules limit what is possible; savings must respect them.
- **Implementation.** Pilot in one region or warehouse, compare against a control, measure, then roll out. Each initiative has an owner, a target and a review date.
- **Expected outcomes and risks.** Lower cost per unit and better capacity use, but beware of local optimisation (cheaper transport but slower delivery) and one-off savings that are not repeatable.
- **Questions.** Who owns the budget and decides on trade-offs? Which service levels are non-negotiable? How much data history is available?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
- **Why integrate.** Finance systems are the source of truth for actual costs, budgets and the ledger. Integrating avoids manual re-entry and spreadsheets, gives one consistent set of numbers and makes the Cost Control Tool usable for month-end close and audits.
- **Benefits.** Fewer errors, faster close, near real-time visibility of overspend, and traceability from a report line down to the originating invoice or transaction.
- **Key questions.** Which systems (ERP, AP/AR, payroll) and which direction of data flow? Real time or batch is enough? Who is the master for cost centres and codes? What is the volume and what are the availability constraints of the finance system?
- **Approach.** Define a stable contract (OpenAPI/event schema, versioned) and a shared master data mapping, e.g. Business Unit Code to cost centre. Prefer events/messages (publish after commit, like we do for the legacy Store sync, with an outbox for reliability) over direct synchronous calls so a finance outage does not block operations.
- **Reliability.** Idempotent consumers, retries with back-off, dead-letter queues, reconciliation jobs that compare totals between systems and alerting when they differ.
- **Security and compliance.** Least-privilege access, audit logging, data retention and segregation of duties.
- **Legacy.** The existing legacy store system is a similar integration, so the same pattern (confirmed data only, after commit) applies.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
- **Why.** Budgets set the spending targets and forecasts show whether we will hit them, so decisions (hiring, capacity, new warehouse locations, replacements) can be made ahead of time instead of reacting to variances.
- **Inputs to take into account.** Historical costs by warehouse/store/product, seasonality and demand forecasts, planned changes (new or replaced warehouses, new stores, contract renewals), capacity and location limits, inflation/fuel/labour rates, and known one-offs.
- **Design.** Plan vs actual at the same granularity as cost tracking so variances are meaningful; versioned budgets and scenarios (base, optimistic, pessimistic); driver-based forecasting (cost per order x expected orders) which is easier to explain than a black-box model; ability to re-forecast during the year.
- **Accuracy and trust.** Measure forecast error (e.g. MAPE) and publish it; allow manual overrides with an audit trail; clearly separate facts from assumptions.
- **Questions.** Planning horizon and frequency? Who approves and owns the budget? Which decisions will actually use the forecast? Is there already a demand forecasting system we can reuse?
- **Risks.** Poor quality of history, forecasting that nobody uses, and complexity that outgrows the value; start simple and improve iteratively.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
- **Why history must be preserved.** The old warehouse is archived but not deleted: its costs, stock movements and performance are the benchmark for the new one, they are needed for audits and for reports of past periods, and they let us calculate the true cost of the replacement (migration, double running, write-offs).
- **How the model supports it.** The Business Unit Code is re-used, so the code identifies the business unit across time, while each warehouse record (technical id, createdAt/archivedAt) identifies a period of it. Cost entries should reference the warehouse record as well as the code, so costs before the replacement stay with the old record and costs after it go to the new one, and both can be combined by code when we want the full history.
- **Budget control.** The new warehouse should get a budget derived from the old one's actual costs (adjusted for the new capacity), with explicit tracking of one-off replacement costs (transition, overlap, decommissioning) separately from run costs so they do not distort the comparison.
- **Rules that interact.** The replacement checks (stock must match and capacity must accommodate it) mean no stock is lost or inflated, which keeps inventory valuation correct in the cost records.
- **Questions.** Are there cut-over rules for costs in the overlap period? Who signs off the budget of the new warehouse? How long is cost history retained, and are there legal requirements? Should reports show the business unit as a continuous series?
- **Success measure.** After the replacement, cost per unit/order is at or below the old warehouse at equal service level within an agreed period.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
