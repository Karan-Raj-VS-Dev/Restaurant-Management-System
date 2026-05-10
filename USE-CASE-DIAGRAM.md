# Use Case Diagram

This diagram shows the primary actors and major system capabilities across dine-in, administration, and delivery workflows.

```mermaid
flowchart LR
  Customer([Customer])
  Waiter([Waiter / Host])
  Cook([Cook])
  Cashier([Cashier])
  Cleaner([Cleaner / Busser])
  Manager([Manager / Admin])
  StaffUser([Staff User])
  Marketplace([Marketplace App])

  subgraph RMS["Restaurant Management System"]
    U0("Login / Recover Password")
    U1("Assign Table")
    U2("View Stock-Aware Menu")
    U3("Create Dine-In Order")
    U4("Track Kitchen Status")
    U5("Prepare Food")
    U6("Generate Bill")
    U7("Process Payment")
    U8("Submit Review")
    U9("Clean And Reset Table")
    U10("Manage Inventory")
    U11("Manage Menu And Recipes")
    U12("Manage Employees")
    U13("Manage Properties And Tables")
    U14("View Reports And Insights")
    U15("Import Marketplace Order")
    U16("Manage Takeaway Order")
    U17("Create / Edit / Delete Tenant Users")
    U18("Force First Login Password Change")
  end

  Manager --> U0
  StaffUser --> U0
  Customer --> U2
  Customer --> U8
  Waiter --> U1
  Waiter --> U2
  Waiter --> U3
  Waiter --> U4
  Waiter --> U6
  Waiter --> U7
  Cook --> U4
  Cook --> U5
  Cashier --> U6
  Cashier --> U7
  Cleaner --> U9
  Manager --> U10
  Manager --> U11
  Manager --> U12
  Manager --> U13
  Manager --> U14
  Manager --> U17
  Marketplace --> U15
  Marketplace --> U16
  StaffUser --> U18
```

## Key Notes

- The dine-in core is handled by `table-service`, `catalog-service`, `inventory-service`, `order-service`, `kitchen-service`, `billing-service`, and `payment-service`.
- Reviews should stay in `review-feedback-service`, not in billing.
- Marketplace orders should enter through `marketplace-integration-service` and then move into `takeaway-service`.
- Authentication and admin user control are handled by `auth-service` and surfaced through `admin-ui`.
- There is no public sign-up page; tenant users are created by admins only.
- Operational use cases run in the `chefy -> tenant -> property` hierarchy, and runtime DB reads should be filtered by `tenant_id` and `property_id`.
