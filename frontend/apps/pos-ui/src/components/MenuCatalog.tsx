import { Button, StatusPill } from "@restaurant/ui";
import type { MenuAvailability, MenuItem } from "@restaurant/api";
import { useState } from "react";

export function MenuCatalog(props: {
  items: MenuItem[];
  availability: Map<string, MenuAvailability>;
  onAdd: (item: MenuItem) => void;
}) {
  const [query, setQuery] = useState("");
  const normalized = query.trim().toLowerCase();
  const filtered = props.items.filter((item) => item.name.toLowerCase().includes(normalized));

  return (
    <div className="pos-menu-panel">
      <input
        className="pos-search"
        placeholder="Search by dish name"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
      />
      <div className="pos-menu-grid">
        {filtered.map((item) => {
          const availability = props.availability.get(item.itemId);
          const enabled = availability?.available ?? item.available;
          return (
            <article key={item.itemId} className="pos-menu-card">
              <div className="pos-menu-card-head">
                <div>
                  <h3>{item.name}</h3>
                  <p>Rs {item.price}</p>
                </div>
                <StatusPill tone={enabled ? "success" : "warning"}>{enabled ? "In stock" : "Unavailable"}</StatusPill>
              </div>
              <p className="pos-menu-reason">{availability?.reason ?? "Live stock sync ready"}</p>
              <div className="pos-menu-ingredients">
                {item.recipe.map((ingredient) => (
                  <span key={ingredient.ingredientId}>{ingredient.name}</span>
                ))}
              </div>
              <Button disabled={!enabled} onClick={() => props.onAdd(item)}>
                Add to order
              </Button>
            </article>
          );
        })}
      </div>
    </div>
  );
}
