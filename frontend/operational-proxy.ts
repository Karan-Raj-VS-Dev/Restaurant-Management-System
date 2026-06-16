const serviceTargets = {
  auth: 9001,
  customer: 9002,
  employee: 9003,
  property: 9004,
  table: 9005,
  catalog: 9006,
  inventory: 9007,
  order: 9008,
  kitchen: 9009,
  billing: 9010,
  payment: 9011,
  insights: 9017
} as const;

type ServiceName = keyof typeof serviceTargets;

export function createOperationalProxy(services: ServiceName[]) {
  return Object.fromEntries(
    services.map((service) => [
      `/services/${service}`,
      {
        target: `http://localhost:${serviceTargets[service]}`,
        changeOrigin: true,
        rewrite: (path: string) => path.replace(new RegExp(`^/services/${service}`), "")
      }
    ])
  );
}
