export const PRODUCT_SLUG = "chefy";
export const DEFAULT_TENANT_ID = "bikini-bottom";
export const DEFAULT_PROPERTY_ID = "krusty-krab";
export const RESTAURANT_SESSION_KEY = "restaurant.ui.session";
export const RESTAURANT_PROPERTY_KEY = "restaurant.ui.selectedProperty";

const RUNTIME_SCOPE_KEY = "restaurant.runtime.scope";
const DASHBOARD_SESSION_GATE_PREFIX = "restaurant.dashboard.session.";

export type FrontendAppId =
  | "admin-ui"
  | "restaurant-ui"
  | "pos-ui"
  | "kitchen-ui"
  | "inventory-ui"
  | "employees-ui"
  | "property-settings-ui"
  | "reports-ui";

interface AuthBootstrapOptions {
  allowDashboardHandoff?: boolean;
}

let runtimeScopeMemory: RuntimeScope | null = null;

export interface RuntimeScope {
  productSlug: string;
  tenantId: string;
  propertyId: string;
}

export type TableStatus = "AVAILABLE" | "UNAVAILABLE" | "RESERVED" | "OCCUPIED" | "NEEDS_CLEANING";
export type KitchenStatus = "RECEIVED" | "ACCEPTED" | "PREPARING" | "READY" | "SERVED";
export type BillStatus = "DRAFT" | "FINALIZED" | "PAID";
export type PaymentMethod = "CASH" | "CARD" | "UPI" | "WALLET";

export interface TableRecord {
  tableId: string;
  tableNumber: string;
  displayName: string;
  propertyId: string;
  floorName: string | null;
  sectionName: string | null;
  capacity: number;
  status: TableStatus;
  waiterId: string | null;
  cleanerId: string | null;
  currentPartySize: number | null;
  reservationPartySize: number | null;
  reservationTime: string | null;
  pendingStatus: TableStatus | null;
  pendingStatusAt: string | null;
}

export interface EmployeeRecord {
  employeeId: string;
  tenantId?: string;
  propertyId: string;
  name: string;
  role: string;
  available: boolean;
  email?: string | null;
  phoneNumber?: string | null;
  shiftName?: string | null;
  salaryAmount?: number | null;
  employmentStatus?: string;
  hireDate?: string | null;
}

export interface MenuItem {
  itemId: string;
  propertyId: string;
  name: string;
  categoryId: string | null;
  categoryName: string | null;
  price: number;
  available: boolean;
  recipe: Array<{ ingredientId: string; name: string; quantity: string }>;
}

export interface MenuAvailability {
  itemId: string;
  propertyId: string;
  available: boolean;
  reason: string;
}

export interface OrderLine {
  itemId: string;
  itemName: string;
  quantity: number;
}

export interface OrderRecord {
  orderId: string;
  propertyId: string;
  tableId: string;
  waiterId: string;
  status: string;
  items: OrderLine[];
  createdAt: string;
}

export interface BillRecord {
  billId: string;
  orderId: string;
  orderIds?: string[];
  tableId: string | null;
  status: BillStatus;
  items: Array<{ itemId: string; itemName: string; quantity: number; unitPrice: number }>;
  subtotal: number;
  tax: number;
  total: number;
}

export interface MenuOrderValidationIssue {
  itemId: string;
  itemName: string;
  requestedQuantity: number;
  maxServableQuantity: number;
  shortageIngredients: string[];
  message: string;
}

export interface MenuOrderValidationResponse {
  valid: boolean;
  issues: MenuOrderValidationIssue[];
}

export interface KitchenTicket {
  ticketId: string;
  orderId: string;
  propertyId: string;
  cookId: string;
  status: KitchenStatus;
  updatedAt: string;
}

export interface KitchenTicketDetail {
  ticketId: string;
  orderId: string;
  tableId: string | null;
  tableName: string;
  tableNumber: string;
  waiterId: string | null;
  waiterName: string;
  cookId: string;
  cookName: string;
  items: OrderLine[];
  status: KitchenStatus;
  updatedAt: string;
}

export interface StockItem {
  ingredientId: string;
  propertyId: string;
  ingredientName: string;
  onHandQuantity: number;
  unit: string;
  reorderThreshold: number;
  maximumCapacity: number;
  stockHealth: string;
}

export interface DailyInsight {
  propertyId: string;
  totalOrdersToday: number;
  busiestTableId: string;
  topServerId: string;
  topServerCustomerCount: number;
  grossSalesToday: number;
}

export interface StockInsight {
  propertyId: string;
  ingredientName: string;
  stockHealth: string;
  availableQuantity: number;
  unit: string;
}

export interface AuthSession {
  userId: string;
  tenantId: string;
  defaultPropertyId: string | null;
  mappedPropertyIds: string[];
  username: string;
  fullName: string;
  adminUser: boolean;
  mustChangePassword: boolean;
  landingPage: string;
}

export interface AuthBootstrapResult {
  authenticated: boolean;
  session: AuthSession | null;
}

export interface PropertyRecord {
  tenantId: string;
  productSlug: string;
  propertyId: string;
  name: string;
  city: string;
  state: string | null;
  country: string;
  addressLine: string | null;
  latitude: number | null;
  longitude: number | null;
  status: string;
}

export interface PasswordResetRequestResult {
  accountFound: boolean;
  message: string;
  deliveryChannel: string | null;
  deliveryHint: string | null;
  devOtp: string | null;
}

export interface PasswordUpdateResult {
  username: string;
  message: string;
}

export interface ManagedUserRecord {
  userId: string;
  tenantId: string;
  defaultPropertyId: string | null;
  mappedPropertyIds: string[];
  firstName: string;
  lastName: string;
  fullName: string;
  username: string;
  email: string;
  phoneCountryCode: string;
  phoneNumber: string;
  addressLine: string;
  latitude: number | null;
  longitude: number | null;
  status: string;
  adminUser: boolean;
  mustChangePassword: boolean;
  lastLoginAt: string | null;
  createdAt: string;
}

export interface ManagedUsersSnapshot {
  totalUsers: number;
  users: ManagedUserRecord[];
}

export interface ApiErrorPayload {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}

export interface ManagedUserPayload {
  firstName: string;
  lastName: string;
  username: string;
  temporaryPassword: string;
  phoneCountryCode: string;
  phoneNumber: string;
  email: string;
  addressLine: string;
  mappedPropertyIds: string[];
  latitude: number | null;
  longitude: number | null;
}

export interface ManagedUserUpdatePayload {
  firstName: string;
  lastName: string;
  username: string;
  temporaryPassword?: string;
  phoneCountryCode: string;
  phoneNumber: string;
  email: string;
  addressLine: string;
  mappedPropertyIds?: string[];
  latitude: number | null;
  longitude: number | null;
  status: string;
}

export interface ManagedPropertyPayload {
  name: string;
  addressLine: string;
  city: string;
  state: string;
  country: string;
  timezone: string;
  latitude: number | null;
  longitude: number | null;
}

export interface ManagedPropertyUpdatePayload {
  name: string;
  addressLine: string;
  city: string;
  state: string;
  country: string;
  timezone: string;
  latitude: number | null;
  longitude: number | null;
  status: string;
}

export interface InventoryDashboardSnapshot {
  stock: StockItem[];
  stockInsights: StockInsight[];
  dailyInsight: DailyInsight;
}

export interface ReportsDashboardSnapshot {
  dailyInsight: DailyInsight;
  orders: OrderRecord[];
  bills: BillRecord[];
  stockInsights: StockInsight[];
  employees: EmployeeRecord[];
}

export interface ManagedEmployeePayload {
  name: string;
  role: string;
  email: string | null;
  phoneNumber: string | null;
  shiftName: string;
  salaryAmount: number;
  available: boolean;
  employmentStatus: string;
}

export interface PropertySettingsModule {
  moduleId: string;
  title: string;
  description: string;
  ownerService: string;
  highlights: string[];
  placeholder: boolean;
}

export interface ImportWorkbookSheet {
  sheetName: string;
  purpose: string;
  requiredColumns: string[];
}

export interface PropertySettingsOverview {
  tenantId: string;
  propertyId: string;
  modules: PropertySettingsModule[];
  importWorkbookSheets: ImportWorkbookSheet[];
}

export interface TableSettingsSummary {
  configuredTables: number;
  editableFields: string[];
  tables: TableSettingRecord[];
}

export interface TableSettingRecord {
  tableId: string;
  tableNumber: string;
  displayName: string;
  floorName: string;
  sectionName: string;
  capacity: number;
  status: string;
  active: boolean;
}

export interface MenuSettingsItem {
  menuItemId: string;
  itemCode: string;
  itemName: string;
  categoryId: string | null;
  categoryName: string | null;
  description: string;
  price: number;
  recipeCount: number;
  active: boolean;
  vegetarian: boolean;
  prepTimeMinutes: number;
  recipe: Array<{ ingredientId: string; name: string; quantity: string }>;
}

export interface MenuSettingsSummary {
  activeMenuItems: number;
  recipeLinks: number;
  editableFields: string[];
  items: MenuSettingsItem[];
}

export interface IngredientSettingItem {
  ingredientId: string;
  ingredientCode: string;
  ingredientName: string;
  unit: string;
  reorderThreshold: number;
  maximumCapacity: number;
  marketPrice: number;
  status: string;
}

export interface SupplySettingItem {
  supplyId: string;
  supplyCode: string;
  supplyName: string;
  unit: string;
  reorderLevel: number;
  marketPrice: number;
  status: string;
}

export interface AreaSectionSettingRecord {
  areaSectionId: string;
  floorName: string;
  sectionName: string;
  maxTableCount: number;
  waiterNames: string[];
  cleanerNames: string[];
  status: string;
}

export interface AreaSectionSettingsSummary {
  editableFields: string[];
  records: AreaSectionSettingRecord[];
}

export interface InventorySettingsSummary {
  ingredients: IngredientSettingItem[];
  supplies: SupplySettingItem[];
  ingredientFields: string[];
  supplyFields: string[];
}

export interface TaxSettingRecord {
  taxId: string;
  taxName: string;
  ratePercent: number;
  status: string;
  appliesTo: string;
}

export interface BillingTemplateRecord {
  templateId: string;
  templateName: string;
  description: Record<string, unknown>;
  status: string;
}

export interface TableSettingPayload {
  tableNumber: string;
  displayName: string;
  floorName: string;
  sectionName: string;
  capacity: number;
  status: string;
  active: boolean;
}

export interface TableStatusChangePayload {
  targetStatus: TableStatus;
  partySize?: number | null;
  waiterId?: string | null;
  cleanerId?: string | null;
  reservationPartySize?: number | null;
  reservationTime?: string | null;
  immediate?: boolean;
  overrideReservationWarning?: boolean;
}

export interface MenuSettingPayload {
  itemCode: string;
  itemName: string;
  categoryName: string;
  description: string;
  price: number;
  vegetarian: boolean;
  prepTimeMinutes: number;
  status: string;
  recipe: RecipeIngredientPayload[];
}

export interface IngredientSettingPayload {
  ingredientCode: string;
  ingredientName: string;
  unit: string;
  reorderThreshold: number;
  maximumCapacity: number;
  marketPrice: number;
  status: string;
}

export interface InventoryStockAdjustmentPayload {
  adjustments: Array<{
    ingredientId: string;
    quantityDelta: number;
  }>;
  reason: string;
}

export interface InventoryStockImportPayload {
  fileName: string;
  fileContent: string;
}

export interface SupplySettingPayload {
  supplyCode: string;
  supplyName: string;
  unit: string;
  reorderLevel: number;
  marketPrice: number;
  status: string;
}

export interface RecipeIngredientPayload {
  ingredientId: string;
  ingredientName: string;
  quantity: string;
}

export interface AreaSectionSettingPayload {
  floorName: string;
  sectionName: string;
  maxTableCount: number;
  waiterNames: string[];
  cleanerNames: string[];
  status: string;
}

export interface TaxSettingPayload {
  taxId: string;
  taxName: string;
  ratePercent: number;
  appliesTo: string;
  status: string;
}

export interface BillingTemplatePayload {
  templateId: string;
  templateName: string;
  description: Record<string, unknown>;
  status: string;
}

export interface BillingSettingsSummary {
  taxes: TaxSettingRecord[];
  templates: BillingTemplateRecord[];
}

export class ApiRequestError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = "ApiRequestError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

export function isApiRequestError(error: unknown): error is ApiRequestError {
  return error instanceof ApiRequestError;
}

export class FrontendSessionExpiredError extends Error {
  appId: FrontendAppId;

  constructor(appId: FrontendAppId, message: string) {
    super(message);
    this.name = "FrontendSessionExpiredError";
    this.appId = appId;
  }
}

export function isFrontendSessionExpiredError(error: unknown): error is FrontendSessionExpiredError {
  return error instanceof FrontendSessionExpiredError;
}

export interface PosSnapshot {
  employees: EmployeeRecord[];
  tables: TableRecord[];
  areaSections: AreaSectionSettingRecord[];
  menu: MenuItem[];
  availability: MenuAvailability[];
  tickets: KitchenTicket[];
  orders: OrderRecord[];
  bills: BillRecord[];
  dailyInsight: DailyInsight;
}

export interface KitchenSnapshot {
  tickets: KitchenTicket[];
  orders: OrderRecord[];
  tables: TableRecord[];
  employees: EmployeeRecord[];
  stock: StockItem[];
  dailyInsight: DailyInsight;
}

export interface PickupQueueItem {
  ticketId: string;
  orderId: string;
  tableId: string | null;
  tableName: string;
  tableNumber: string;
  waiterId: string | null;
  waiterName: string;
  items: OrderLine[];
  status: KitchenStatus;
  updatedAt: string;
}

export function getRuntimeScope(): RuntimeScope {
  if (typeof window === "undefined") {
    return { productSlug: PRODUCT_SLUG, tenantId: DEFAULT_TENANT_ID, propertyId: DEFAULT_PROPERTY_ID };
  }

  if (runtimeScopeMemory) {
    return runtimeScopeMemory;
  }

  const raw = window.sessionStorage.getItem(RUNTIME_SCOPE_KEY);
  if (!raw) {
    return { productSlug: PRODUCT_SLUG, tenantId: DEFAULT_TENANT_ID, propertyId: DEFAULT_PROPERTY_ID };
  }

  try {
    const parsed = JSON.parse(raw) as Partial<RuntimeScope>;
    runtimeScopeMemory = {
      productSlug: parsed.productSlug || PRODUCT_SLUG,
      tenantId: parsed.tenantId || DEFAULT_TENANT_ID,
      propertyId: parsed.propertyId || DEFAULT_PROPERTY_ID
    };
    return runtimeScopeMemory;
  } catch {
    return { productSlug: PRODUCT_SLUG, tenantId: DEFAULT_TENANT_ID, propertyId: DEFAULT_PROPERTY_ID };
  }
}

export function buildKitchenTicketDetails(
  tickets: KitchenTicket[],
  orders: OrderRecord[],
  tables: TableRecord[],
  employees: EmployeeRecord[]
): KitchenTicketDetail[] {
  const orderById = new Map(orders.map((order) => [order.orderId, order]));
  const tableById = new Map(tables.map((table) => [table.tableId, table]));
  const employeeNameById = new Map(employees.map((employee) => [employee.employeeId, employee.name]));

  return tickets
    .map((ticket) => {
      const order = orderById.get(ticket.orderId);
      const table = order?.tableId ? tableById.get(order.tableId) ?? null : null;
      const waiterId = order?.waiterId ?? table?.waiterId ?? null;
      return {
        ticketId: ticket.ticketId,
        orderId: ticket.orderId,
        tableId: order?.tableId ?? table?.tableId ?? null,
        tableName: table?.displayName ?? order?.tableId ?? "Walk-in order",
        tableNumber: table?.tableNumber ?? order?.tableId ?? "Table pending",
        waiterId,
        waiterName: waiterId ? employeeNameById.get(waiterId) ?? waiterId : "Server pending",
        cookId: ticket.cookId,
        cookName:
          ticket.cookId && ticket.cookId !== "cook-pending"
            ? employeeNameById.get(ticket.cookId) ?? ticket.cookId
            : "Chef pending",
        items: order?.items ?? [],
        status: ticket.status,
        updatedAt: ticket.updatedAt
      } satisfies KitchenTicketDetail;
    })
    .sort((left, right) => new Date(left.updatedAt).getTime() - new Date(right.updatedAt).getTime());
}

export function buildPickupQueue(
  tickets: KitchenTicket[],
  orders: OrderRecord[],
  tables: TableRecord[],
  employees: EmployeeRecord[]
): PickupQueueItem[] {
  return buildKitchenTicketDetails(tickets, orders, tables, employees)
    .filter((ticket) => ticket.status === "READY")
    .map((ticket) => ({
      ticketId: ticket.ticketId,
      orderId: ticket.orderId,
      tableId: ticket.tableId,
      tableName: ticket.tableName,
      tableNumber: ticket.tableNumber,
      waiterId: ticket.waiterId,
      waiterName: ticket.waiterName,
      items: ticket.items,
      status: ticket.status,
      updatedAt: ticket.updatedAt
    }))
    .sort((left, right) => new Date(left.updatedAt).getTime() - new Date(right.updatedAt).getTime());
}

export function saveRuntimeScope(scope: RuntimeScope) {
  if (typeof window === "undefined") {
    return;
  }
  runtimeScopeMemory = scope;
  window.sessionStorage.setItem(RUNTIME_SCOPE_KEY, JSON.stringify(scope));
}

export function clearRuntimeScope() {
  if (typeof window === "undefined") {
    return;
  }
  runtimeScopeMemory = null;
  window.sessionStorage.removeItem(RUNTIME_SCOPE_KEY);
}

export function loadStoredRestaurantSession(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const raw = window.sessionStorage.getItem(RESTAURANT_SESSION_KEY);
    return raw ? (JSON.parse(raw) as AuthSession) : null;
  } catch {
    return null;
  }
}

export function loadStoredRestaurantProperty(): PropertyRecord | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const raw = window.sessionStorage.getItem(RESTAURANT_PROPERTY_KEY);
    return raw ? (JSON.parse(raw) as PropertyRecord) : null;
  } catch {
    return null;
  }
}

export function clearStoredRestaurantSelection() {
  if (typeof window === "undefined") {
    return;
  }
  window.sessionStorage.removeItem(RESTAURANT_PROPERTY_KEY);
}

export function clearStoredRestaurantSession() {
  if (typeof window === "undefined") {
    return;
  }
  window.sessionStorage.removeItem(RESTAURANT_SESSION_KEY);
}

export async function bootstrapAuthenticatedSession(
  appId: FrontendAppId,
  options: AuthBootstrapOptions = {}
): Promise<AuthBootstrapResult> {
  if (typeof window === "undefined") {
    return { authenticated: false, session: null };
  }

  const usesPersistentSessionGate = usesPersistentSession(appId);
  const hadLocalSession = usesPersistentSessionGate && !!loadStoredRestaurantSession();
  const hadDashboardSession = !usesPersistentSessionGate && hasDashboardSessionGate(appId);
  const hadDashboardHandoff = !usesPersistentSessionGate && (options.allowDashboardHandoff === true || hadDashboardSession);

  if (usesPersistentSessionGate && !hadLocalSession) {
    return { authenticated: false, session: null };
  }

  if (!usesPersistentSessionGate && !hadDashboardHandoff) {
    return { authenticated: false, session: null };
  }

  try {
    const session = await fetchJson<AuthSession>("/services/auth/api/auth/session");
    if (usesPersistentSessionGate) {
      persistRestaurantSession(session);
    } else {
      persistDashboardSessionGate(appId);
    }
    return { authenticated: true, session };
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 401 || error.status === 403)) {
      clearFrontendAuthState(appId);
      return { authenticated: false, session: null };
    }
    if (hadLocalSession || hadDashboardHandoff) {
      clearFrontendAuthState(appId);
      throw new FrontendSessionExpiredError(appId, sessionExpiredMessage(appId));
    }
    throw error;
  }
}

export async function logoutFrontendSession(appId: FrontendAppId, postLogoutUrl?: string) {
  try {
    await fetchJson<void>("/services/auth/api/auth/logout", undefined, {
      method: "POST",
      skipAuth: true
    });
  } finally {
    clearFrontendAuthState(appId);
    if (typeof window !== "undefined" && postLogoutUrl && window.location.href !== postLogoutUrl) {
      window.location.assign(postLogoutUrl);
    }
  }
}

export function clearFrontendAuthState(appId: FrontendAppId) {
  if (typeof window === "undefined") {
    return;
  }
  window.sessionStorage.removeItem(RESTAURANT_SESSION_KEY);
  clearStoredRestaurantSelection();
  clearRuntimeScope();
  clearDashboardSessionGate(appId);
  clearAllDashboardSessionGates();
}

function persistRestaurantSession(session: AuthSession) {
  if (typeof window === "undefined") {
    return;
  }
  window.sessionStorage.setItem(RESTAURANT_SESSION_KEY, JSON.stringify(session));
}

function dashboardSessionGateKey(appId: FrontendAppId) {
  return `${DASHBOARD_SESSION_GATE_PREFIX}${appId}`;
}

function hasDashboardSessionGate(appId: FrontendAppId) {
  if (typeof window === "undefined" || usesPersistentSession(appId)) {
    return false;
  }
  return window.sessionStorage.getItem(dashboardSessionGateKey(appId)) === "1";
}

function persistDashboardSessionGate(appId: FrontendAppId) {
  if (typeof window === "undefined" || usesPersistentSession(appId)) {
    return;
  }
  window.sessionStorage.setItem(dashboardSessionGateKey(appId), "1");
}

function clearDashboardSessionGate(appId: FrontendAppId) {
  if (typeof window === "undefined" || usesPersistentSession(appId)) {
    return;
  }
  window.sessionStorage.removeItem(dashboardSessionGateKey(appId));
}

function clearAllDashboardSessionGates() {
  if (typeof window === "undefined") {
    return;
  }
  const dashboardApps: FrontendAppId[] = [
    "pos-ui",
    "kitchen-ui",
    "inventory-ui",
    "employees-ui",
    "property-settings-ui",
    "reports-ui"
  ];
  for (const dashboardApp of dashboardApps) {
    window.sessionStorage.removeItem(dashboardSessionGateKey(dashboardApp));
  }
}

function usesPersistentSession(appId: FrontendAppId) {
  return appId === "admin-ui" || appId === "restaurant-ui";
}

function sessionExpiredMessage(appId: FrontendAppId) {
  if (appId === "admin-ui") {
    return "Your admin login expired. Please sign in again.";
  }
  if (appId === "restaurant-ui") {
    return "Your restaurant login expired. Please sign in again.";
  }
  return "Your login expired. Please sign in again.";
}

function tenantApiUrl(service: string, path: string, scope: RuntimeScope = getRuntimeScope()) {
  return `/services/${service}/${scope.productSlug}/tenant/${scope.tenantId}${path}`;
}

function propertyApiUrl(service: string, path: string, scope: RuntimeScope = getRuntimeScope()) {
  return `/services/${service}/${scope.productSlug}/tenant/${scope.tenantId}/property/${scope.propertyId}${path}`;
}

export async function loadPosSnapshot(): Promise<PosSnapshot> {
  const [employees, tables, areaSections, menu, availability, tickets, orders, bills, dailyInsight] = await Promise.all([
    fetchJson<EmployeeRecord[]>(propertyApiUrl("employee", "/api/employees"), []),
    fetchJson<TableRecord[]>(propertyApiUrl("table", "/api/tables"), []),
    fetchJson<AreaSectionSettingsSummary>(propertyApiUrl("property", "/api/settings/areas-sections"), { editableFields: [], records: [] }),
    fetchJson<MenuItem[]>(propertyApiUrl("catalog", "/api/menu/items"), []),
    fetchJson<MenuAvailability[]>(propertyApiUrl("inventory", "/api/inventory/availability/menu-items"), []),
    fetchJson<KitchenTicket[]>(propertyApiUrl("kitchen", "/api/kitchen/tickets"), []),
    fetchJson<OrderRecord[]>(propertyApiUrl("order", "/api/orders"), []),
    fetchJson<BillRecord[]>(propertyApiUrl("billing", "/api/bills"), []),
    fetchJson<DailyInsight>(propertyApiUrl("insights", "/api/insights/daily"), fallbackInsight)
  ]);

  return { employees, tables, areaSections: areaSections.records, menu, availability, tickets, orders, bills, dailyInsight };
}

export async function assignTableToWaiter(args: {
  tableId: string;
  propertyId: string;
  capacity: number;
  waiterId: string;
}) {
  return fetchJson<TableRecord>(propertyApiUrl("table", "/api/tables/assign"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(args)
  });
}

export async function updateTableStatus(tableId: string, payload: TableStatusChangePayload) {
  return fetchJson<TableRecord>(propertyApiUrl("table", `/api/tables/${tableId}/status`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function loadKitchenSnapshot(): Promise<KitchenSnapshot> {
  const [tickets, orders, tables, employees, stock, dailyInsight] = await Promise.all([
    fetchJson<KitchenTicket[]>(propertyApiUrl("kitchen", "/api/kitchen/tickets"), []),
    fetchJson<OrderRecord[]>(propertyApiUrl("order", "/api/orders"), []),
    fetchJson<TableRecord[]>(propertyApiUrl("table", "/api/tables"), []),
    fetchJson<EmployeeRecord[]>(propertyApiUrl("employee", "/api/employees"), []),
    fetchJson<StockItem[]>(propertyApiUrl("inventory", "/api/inventory/stock"), []),
    fetchJson<DailyInsight>(propertyApiUrl("insights", "/api/insights/daily"), fallbackInsight)
  ]);

  return { tickets, orders, tables, employees, stock, dailyInsight };
}

export async function createDineInOrder(args: {
  tableId: string;
  waiterId: string;
  items: OrderLine[];
}) {
  const scope = getRuntimeScope();
  const order = await fetchJson<OrderRecord>(propertyApiUrl("order", "/api/orders"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      propertyId: scope.propertyId,
      tableId: args.tableId,
      waiterId: args.waiterId,
      items: args.items
    })
  });

  await fetchJson<OrderRecord>(propertyApiUrl("order", `/api/orders/${order.orderId}/submit-to-kitchen`), order, {
    method: "PATCH"
  });

  return order;
}

export async function validateDineInOrder(items: OrderLine[]) {
  return fetchJson<MenuOrderValidationResponse>(propertyApiUrl("inventory", "/api/inventory/availability/menu-items/validate-order"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      items: items.map((item) => ({
        itemId: item.itemId,
        itemName: item.itemName,
        quantity: item.quantity
      }))
    })
  });
}

export async function loginWithCredentials(username: string, password: string) {
  const session = await fetchJson<AuthSession>(tenantApiUrl("auth", "/api/auth/login"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
    skipAuth: true
  });
  persistRestaurantSession(session);
  return session;
}

export async function requestPasswordResetOtp(identifier: string) {
  return fetchJson<PasswordResetRequestResult>(tenantApiUrl("auth", "/api/auth/password-reset/request"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ identifier }),
    skipAuth: true
  });
}

export async function confirmPasswordResetOtp(args: { identifier: string; otp: string; newPassword: string }) {
  return fetchJson<PasswordUpdateResult>(tenantApiUrl("auth", "/api/auth/password-reset/confirm"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(args),
    skipAuth: true
  });
}

export async function changeOwnPassword(args: { username: string; currentPassword: string; newPassword: string }) {
  return fetchJson<PasswordUpdateResult>(tenantApiUrl("auth", "/api/auth/password-change"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(args),
    skipAuth: true
  });
}

export async function loadManagedUsers(actorUsername: string) {
  return fetchJson<ManagedUsersSnapshot>(tenantApiUrl("auth", "/api/auth/admin/users"));
}

export async function createManagedUser(actorUsername: string, payload: ManagedUserPayload) {
  return fetchJson<ManagedUserRecord>(tenantApiUrl("auth", "/api/auth/admin/users"), undefined, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
}

export async function updateManagedUser(actorUsername: string, userId: string, payload: ManagedUserUpdatePayload) {
  return fetchJson<ManagedUserRecord>(tenantApiUrl("auth", `/api/auth/admin/users/${userId}`), undefined, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
}

export async function deleteManagedUser(actorUsername: string, userId: string) {
  return fetchJson<void>(tenantApiUrl("auth", `/api/auth/admin/users/${userId}`), undefined, {
    method: "DELETE"
  });
}

export async function loadTenantProperties() {
  return fetchJson<PropertyRecord[]>(tenantApiUrl("property", "/api/properties"), []);
}

export async function createManagedProperty(payload: ManagedPropertyPayload) {
  return fetchJson<PropertyRecord>(tenantApiUrl("property", "/api/properties"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateManagedProperty(propertyId: string, payload: ManagedPropertyUpdatePayload) {
  return fetchJson<PropertyRecord>(tenantApiUrl("property", `/api/properties/${propertyId}`), undefined, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function deleteManagedProperty(propertyId: string) {
  return fetchJson<void>(tenantApiUrl("property", `/api/properties/${propertyId}`), undefined, {
    method: "DELETE"
  });
}

export async function loadInventoryDashboard(): Promise<InventoryDashboardSnapshot> {
  const [stock, stockInsights, dailyInsight] = await Promise.all([
    fetchJson<StockItem[]>(propertyApiUrl("inventory", "/api/inventory/stock"), []),
    fetchJson<StockInsight[]>(propertyApiUrl("insights", "/api/insights/stock"), []),
    fetchJson<DailyInsight>(propertyApiUrl("insights", "/api/insights/daily"), fallbackInsight)
  ]);

  return { stock, stockInsights, dailyInsight };
}

export async function loadReportsDashboard(): Promise<ReportsDashboardSnapshot> {
  const [dailyInsight, orders, bills, stockInsights, employees] = await Promise.all([
    fetchJson<DailyInsight>(propertyApiUrl("insights", "/api/insights/daily"), fallbackInsight),
    fetchJson<OrderRecord[]>(propertyApiUrl("order", "/api/orders"), []),
    fetchJson<BillRecord[]>(propertyApiUrl("billing", "/api/bills"), []),
    fetchJson<StockInsight[]>(propertyApiUrl("insights", "/api/insights/stock"), []),
    fetchJson<EmployeeRecord[]>(propertyApiUrl("employee", "/api/employees"), [])
  ]);

  return { dailyInsight, orders, bills, stockInsights, employees };
}

export async function loadPropertyEmployees() {
  return fetchJson<EmployeeRecord[]>(propertyApiUrl("employee", "/api/employees"));
}

export async function loadPropertySettingsOverview() {
  return fetchJson<PropertySettingsOverview>(propertyApiUrl("property", "/api/settings/overview"));
}

export async function loadTableSettingsSummary() {
  return fetchJson<TableSettingsSummary>(propertyApiUrl("table", "/api/tables/settings/tables"));
}

export async function loadMenuSettingsSummary() {
  return fetchJson<MenuSettingsSummary>(propertyApiUrl("catalog", "/api/menu/settings/menu"));
}

export async function loadInventorySettingsSummary() {
  return fetchJson<InventorySettingsSummary>(propertyApiUrl("inventory", "/api/inventory/settings/inventory"));
}

export async function loadBillingSettingsSummary() {
  return fetchJson<BillingSettingsSummary>(propertyApiUrl("billing", "/api/settings/billing"));
}

export async function loadAreaSectionSettingsSummary() {
  return fetchJson<AreaSectionSettingsSummary>(propertyApiUrl("property", "/api/settings/areas-sections"));
}

export async function createTableSetting(payload: TableSettingPayload) {
  return fetchJson<TableSettingRecord>(propertyApiUrl("table", "/api/tables/settings/tables"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateTableSetting(tableId: string, payload: TableSettingPayload) {
  return fetchJson<TableSettingRecord>(propertyApiUrl("table", `/api/tables/settings/tables/${tableId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createMenuSetting(payload: MenuSettingPayload) {
  return fetchJson<MenuSettingsItem>(propertyApiUrl("catalog", "/api/menu/settings/menu"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateMenuSetting(menuItemId: string, payload: MenuSettingPayload) {
  return fetchJson<MenuSettingsItem>(propertyApiUrl("catalog", `/api/menu/settings/menu/${menuItemId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createIngredientSetting(payload: IngredientSettingPayload) {
  return fetchJson<IngredientSettingItem>(propertyApiUrl("inventory", "/api/inventory/settings/inventory/ingredients"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateIngredientSetting(ingredientId: string, payload: IngredientSettingPayload) {
  return fetchJson<IngredientSettingItem>(propertyApiUrl("inventory", `/api/inventory/settings/inventory/ingredients/${ingredientId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function applyInventoryStockAdjustments(payload: InventoryStockAdjustmentPayload) {
  return fetchJson<StockItem[]>(propertyApiUrl("inventory", "/api/inventory/stock/adjustments"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function importInventoryStockSheet(payload: InventoryStockImportPayload) {
  return fetchJson<StockItem[]>(propertyApiUrl("inventory", "/api/inventory/stock/import"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createSupplySetting(payload: SupplySettingPayload) {
  return fetchJson<SupplySettingItem>(propertyApiUrl("inventory", "/api/inventory/settings/inventory/supplies"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateSupplySetting(supplyId: string, payload: SupplySettingPayload) {
  return fetchJson<SupplySettingItem>(propertyApiUrl("inventory", `/api/inventory/settings/inventory/supplies/${supplyId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createTaxSetting(payload: TaxSettingPayload) {
  return fetchJson<TaxSettingRecord>(propertyApiUrl("billing", "/api/settings/billing/taxes"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateTaxSetting(taxId: string, payload: TaxSettingPayload) {
  return fetchJson<TaxSettingRecord>(propertyApiUrl("billing", `/api/settings/billing/taxes/${taxId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createBillingTemplate(payload: BillingTemplatePayload) {
  return fetchJson<BillingTemplateRecord>(propertyApiUrl("billing", "/api/settings/billing/templates"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateBillingTemplate(templateId: string, payload: BillingTemplatePayload) {
  return fetchJson<BillingTemplateRecord>(propertyApiUrl("billing", `/api/settings/billing/templates/${templateId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createAreaSectionSetting(payload: AreaSectionSettingPayload) {
  return fetchJson<AreaSectionSettingRecord>(propertyApiUrl("property", "/api/settings/areas-sections"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateAreaSectionSetting(areaSectionId: string, payload: AreaSectionSettingPayload) {
  return fetchJson<AreaSectionSettingRecord>(propertyApiUrl("property", `/api/settings/areas-sections/${areaSectionId}`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function createManagedEmployee(payload: ManagedEmployeePayload) {
  return fetchJson<EmployeeRecord>(propertyApiUrl("employee", "/api/employees"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function updateManagedEmployee(employeeId: string, payload: ManagedEmployeePayload) {
  return fetchJson<EmployeeRecord>(propertyApiUrl("employee", `/api/employees/${employeeId}`), undefined, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export async function deleteManagedEmployee(employeeId: string) {
  return fetchJson<void>(propertyApiUrl("employee", `/api/employees/${employeeId}`), undefined, {
    method: "DELETE"
  });
}

export async function finalizeBill(billId: string) {
  return fetchJson<BillRecord>(propertyApiUrl("billing", `/api/bills/${billId}/finalize`), undefined, { method: "POST" });
}

export async function processBillPayment(bill: BillRecord, method: PaymentMethod) {
  return fetchJson(propertyApiUrl("payment", "/api/payments"), undefined, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      billId: bill.billId,
      method,
      amount: bill.total
    })
  });
}

export async function acceptKitchenTicket(ticketId: string, cookId?: string | null) {
  return fetchJson<KitchenTicket>(propertyApiUrl("kitchen", `/api/kitchen/tickets/${ticketId}/accept`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cookId: cookId ?? null })
  });
}

export async function readyKitchenTicket(ticketId: string, cookId?: string | null) {
  return fetchJson<KitchenTicket>(propertyApiUrl("kitchen", `/api/kitchen/tickets/${ticketId}/ready`), undefined, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cookId: cookId ?? null })
  });
}

export async function serveKitchenTicket(ticketId: string) {
  return fetchJson<KitchenTicket>(propertyApiUrl("kitchen", `/api/kitchen/tickets/${ticketId}/served`), undefined, {
    method: "PATCH"
  });
}

export async function markOrderReadyToServe(orderId: string) {
  return fetchJson<OrderRecord>(propertyApiUrl("order", `/api/orders/${orderId}/ready-to-serve`), undefined, {
    method: "PATCH"
  });
}

export async function markOrderServed(orderId: string) {
  return fetchJson<OrderRecord>(propertyApiUrl("order", `/api/orders/${orderId}/served`), undefined, {
    method: "PATCH"
  });
}

export function buildAvailabilityMap(items: MenuAvailability[]) {
  return new Map(items.map((item) => [item.itemId, item]));
}

type ApiFetchInit = RequestInit & { skipAuth?: boolean };

async function fetchJson<T>(url: string, fallback?: T, init?: ApiFetchInit): Promise<T> {
  try {
    const response = await fetch(url, await withAuthHeaders(init));
    if (!response.ok) {
      const payload = await readErrorPayload(response);
      throw new ApiRequestError(
        payload.message || `Request failed with ${response.status}`,
        response.status,
        payload.fieldErrors
      );
    }
    if (response.status === 204) {
      return undefined as T;
    }
    return (await response.json()) as T;
  } catch (error) {
    if (fallback !== undefined) {
      return fallback;
    }
    throw error;
  }
}

async function withAuthHeaders(init?: ApiFetchInit): Promise<RequestInit> {
  const headers = new Headers(init?.headers ?? {});
  return {
    ...init,
    credentials: "include",
    headers
  };
}

async function readErrorPayload(response: Response): Promise<ApiErrorPayload> {
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    try {
      const parsed = (await response.json()) as Partial<ApiErrorPayload> & {
        detail?: string;
        error?: string;
        title?: string;
      };
      return {
        status: response.status,
        message: parsed.message || parsed.detail || parsed.error || parsed.title || `Request failed with ${response.status}`,
        fieldErrors: normalizeFieldErrors(parsed.fieldErrors)
      };
    } catch {
      return {
        status: response.status,
        message: `Request failed with ${response.status}`,
        fieldErrors: {}
      };
    }
  }

  try {
    const message = (await response.text()).trim();
    return {
      status: response.status,
      message: message || `Request failed with ${response.status}`,
      fieldErrors: {}
    };
  } catch {
    return {
      status: response.status,
      message: `Request failed with ${response.status}`,
      fieldErrors: {}
    };
  }
}

function normalizeFieldErrors(raw: unknown): Record<string, string> {
  if (!raw || typeof raw !== "object") {
    return {};
  }

  return Object.entries(raw as Record<string, unknown>).reduce<Record<string, string>>((accumulator, [key, value]) => {
    if (typeof value === "string" && value.trim()) {
      accumulator[key] = value;
    }
    return accumulator;
  }, {});
}

const fallbackInsight: DailyInsight = {
  propertyId: DEFAULT_PROPERTY_ID,
  totalOrdersToday: 0,
  busiestTableId: "",
  topServerId: "",
  topServerCustomerCount: 0,
  grossSalesToday: 0
};
