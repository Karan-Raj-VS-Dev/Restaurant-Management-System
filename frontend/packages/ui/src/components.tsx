import { ReactNode, useEffect, useRef, useState, startTransition } from "react";

export function AppShell(props: {
  eyebrow: string;
  title: string;
  subtitle: string;
  children: ReactNode;
  actions?: ReactNode;
}) {
  return (
    <div className="rm-shell">
      <header className="rm-hero">
        <div>
          <div className="rm-eyebrow">{props.eyebrow}</div>
          <h1 className="rm-title">{props.title}</h1>
          <p className="rm-subtitle">{props.subtitle}</p>
        </div>
        <div className="rm-hero-actions">{props.actions}</div>
      </header>
      <main>{props.children}</main>
    </div>
  );
}

export function SectionCard(props: {
  title: string;
  subtitle?: string;
  children: ReactNode;
  action?: ReactNode;
  className?: string;
}) {
  return (
    <section className={["rm-card", props.className].filter(Boolean).join(" ")}>
      <div className="rm-card-header">
        <div>
          <h2 className="rm-card-title">{props.title}</h2>
          {props.subtitle ? <p className="rm-card-subtitle">{props.subtitle}</p> : null}
        </div>
        {props.action ? <div>{props.action}</div> : null}
      </div>
      <div>{props.children}</div>
    </section>
  );
}

export function StatCard(props: {
  label: string;
  value: string;
  hint?: string;
  tone?: "warm" | "cool" | "neutral" | "alert";
}) {
  return (
    <div className={`rm-stat-card rm-tone-${props.tone ?? "neutral"}`}>
      <div className="rm-stat-label">{props.label}</div>
      <div className="rm-stat-value">{props.value}</div>
      {props.hint ? <div className="rm-stat-hint">{props.hint}</div> : null}
    </div>
  );
}

export function StatusPill(props: {
  children: ReactNode;
  tone?: "success" | "warning" | "danger" | "info" | "muted";
}) {
  return <span className={`rm-pill rm-pill-${props.tone ?? "muted"}`}>{props.children}</span>;
}

export function WorkspaceIdentityBadges(props: {
  userName?: string | null;
  propertyName?: string | null;
}) {
  if (!props.userName && !props.propertyName) {
    return null;
  }

  return (
    <div className="rm-identity-badges">
      {props.userName ? <StatusPill tone="danger">{props.userName}</StatusPill> : null}
      {props.propertyName ? <StatusPill tone="warning">{props.propertyName}</StatusPill> : null}
    </div>
  );
}

export function Button(props: {
  children: ReactNode;
  onClick?: () => void | Promise<void>;
  disabled?: boolean;
  variant?: "primary" | "ghost" | "secondary";
  type?: "button" | "submit";
}) {
  return (
    <button
      type={props.type ?? "button"}
      className={`rm-button rm-button-${props.variant ?? "primary"}`}
      onClick={props.onClick}
      disabled={props.disabled}
    >
      {props.children}
    </button>
  );
}

export function LivePulse(props: { label: string; lastUpdated?: Date | null }) {
  return (
    <div className="rm-live-pulse">
      <span className="rm-live-dot" />
      <span>{props.label}</span>
      <span className="rm-live-timestamp">
        {props.lastUpdated ? `Updated ${formatRelative(props.lastUpdated)}` : "Waiting for data"}
      </span>
    </div>
  );
}

export function EmptyState(props: { title: string; body: string }) {
  return (
    <div className="rm-empty-state">
      <h3>{props.title}</h3>
      <p>{props.body}</p>
    </div>
  );
}

export function usePollingResource<T>(loader: () => Promise<T>, intervalMs: number) {
  const loaderRef = useRef(loader);
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  loaderRef.current = loader;

  useEffect(() => {
    let cancelled = false;

    const refresh = async (markLoading: boolean) => {
      if (markLoading) {
        setLoading(true);
      } else {
        setRefreshing(true);
      }

      try {
        const next = await loaderRef.current();
        if (!cancelled) {
          startTransition(() => {
            setData(next);
            setError(null);
            setLastUpdated(new Date());
          });
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(caughtError instanceof Error ? caughtError.message : "Unknown loading error");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
          setRefreshing(false);
        }
      }
    };

    void refresh(true);
    const timer = window.setInterval(() => {
      void refresh(false);
    }, intervalMs);

    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [intervalMs]);

  return {
    data,
    loading,
    refreshing,
    error,
    lastUpdated,
    refresh: async () => {
      const next = await loaderRef.current();
      startTransition(() => {
        setData(next);
        setError(null);
        setLastUpdated(new Date());
      });
    }
  };
}

function formatRelative(date: Date) {
  const seconds = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000));
  if (seconds < 5) {
    return "just now";
  }
  if (seconds < 60) {
    return `${seconds}s ago`;
  }
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) {
    return `${minutes}m ago`;
  }
  const hours = Math.floor(minutes / 60);
  return `${hours}h ago`;
}
