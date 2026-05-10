# BPMN Diagrams

This repository includes BPMN files for the two most important operational flows.

## Included Files

- [diagrams/dine-in-main-flow.bpmn](./diagrams/dine-in-main-flow.bpmn)
- [diagrams/marketplace-takeaway-flow.bpmn](./diagrams/marketplace-takeaway-flow.bpmn)

## Scope Model

The BPMN flows in this project are executed under a tenant and property scope:

- product slug: `chefy`
- route pattern: `/{productSlug}/tenant/{tenantId}/property/{propertyId}/api/...`
- database lookups: `tenant_id + property_id`
- event payloads: `tenantId + propertyId`

## Dine-In Flow Preview

```mermaid
flowchart LR
  A["Customer arrives"] --> B["Assign table"]
  B --> C["Load stock-aware menu"]
  C --> D["Create dine-in order"]
  D --> E["Create kitchen ticket"]
  E --> F["Reserve ingredients"]
  F --> G{"Stock available?"}
  G -->|No| H["Notify waiter and revise order"]
  H --> C
  G -->|Yes| I["Prepare food"]
  I --> J["Serve food"]
  J --> K["Finalize bill"]
  K --> L["Process payment"]
  L --> M{"Payment successful?"}
  M -->|No| L
  M -->|Yes| N["Collect review"]
  N --> O["Clean and reset table"]
```

## Marketplace Flow Preview

```mermaid
flowchart LR
  A["Marketplace sends order"] --> B["Validate payload"]
  B --> C["Map to internal takeaway order"]
  C --> D["Reserve stock"]
  D --> E{"Stock available?"}
  E -->|No| F["Reject or substitute order"]
  F --> G["Push failure status to marketplace"]
  E -->|Yes| H["Create kitchen ticket"]
  H --> I["Prepare takeaway order"]
  I --> J["Push preparation status"]
  J --> K["Reconcile payment"]
  K --> L["Close takeaway order"]
```

## Modeling Notes

- These BPMN files are meant to be a project starting point, not the final orchestration implementation.
- Later you can extend them with compensation, retries, timeout events, and message events for Kafka-driven execution.
- Flow execution and projections should always stay property-scoped within a tenant.

## Auth And Admin Flow Preview

```mermaid
flowchart LR
  A["User opens admin-ui"] --> B["Enter username and password"]
  B --> C{"Credentials valid?"}
  C -->|No| D["Show login error"]
  C -->|Yes| E{"Must change password?"}
  E -->|Yes| F["Prompt for new password in same SPA"]
  F --> G["Update password in auth-service"]
  E -->|No| H["Open landing page"]
  G --> H
  H --> I["Choose diner / inventory / kitchen / employees / reports"]
  J["Forgot password"] --> K["Enter email or phone"]
  K --> L["Generate OTP"]
  L --> M["Verify OTP"]
  M --> N["Set new password"]
  N --> B
  O["Admin user control"] --> P["Create tenant user with temporary password"]
  P --> Q["Store user in auth.app_users"]
```

This auth flow is implemented in code even though there is not yet a dedicated BPMN XML file for it.
