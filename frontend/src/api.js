const API_BASE = "/api";

export async function request(
  key,
  path,
  { method = "GET", body, idempotencyKey } = {},
) {
  // Separate the query string from the endpoint path.
  const queryIndex = path.indexOf("?");
  const pathname =
    queryIndex === -1 ? path : path.slice(0, queryIndex);
  const query =
    queryIndex === -1 ? "" : path.slice(queryIndex);

  // Match frontend requests to Spring Boot mappings.
  let endpoint = pathname;

  if (pathname === "/transactions") {
    endpoint = "/transactions/list";
  }

  if (pathname === "/orders" && method.toUpperCase() === "POST") {
    endpoint = "/orders/create";
  }

  const response = await fetch(`${API_BASE}${endpoint}${query}`, {
    method,
    headers: {
      ...(body !== undefined
        ? { "Content-Type": "application/json" }
        : {}),
      ...(key ? { "X-Api-Key": key } : {}),
      ...(idempotencyKey
        ? { "Idempotency-Key": idempotencyKey }
        : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const error = new Error(
      data?.message || `Request failed (${response.status})`,
    );
    error.status = response.status;
    throw error;
  }

  return data;
}

export const newKey = () => crypto.randomUUID();

export const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format((value || 0) / 100);

export const date = (value) =>
  new Date(value).toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });

export function paise(value) {
  if (!/^\d+(\.\d{1,2})?$/.test(value)) {
    throw new Error("Enter an amount with up to two decimal places");
  }

  const [rupees, decimal = ""] = value.split(".");
  const amount = Number(rupees) * 100 + Number(decimal.padEnd(2, "0"));

  if (
    !Number.isSafeInteger(amount) ||
    amount < 1 ||
    amount > 100000000
  ) {
    throw new Error("Amount must be between ₹0.01 and ₹10,00,000");
  }

  return amount;
}