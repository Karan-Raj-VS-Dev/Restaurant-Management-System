import { Button, StatusPill } from "@restaurant/ui";
import type { MenuAvailability, MenuItem } from "@restaurant/api";
import { useMemo, useState } from "react";

export function MenuCatalog(props: {
  items: MenuItem[];
  availability: Map<string, MenuAvailability>;
  onAdd: (item: MenuItem) => void;
}) {
  const [query, setQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("ALL");
  const categories = useMemo(
    () =>
      Array.from(
        new Set(
          props.items
            .map((item) => item.categoryName?.trim())
            .filter((value): value is string => Boolean(value))
        )
      ).sort((left, right) => left.localeCompare(right)),
    [props.items]
  );
  const normalized = query.trim().toLowerCase();
  const filtered = props.items.filter((item) => {
    const matchesQuery = item.name.toLowerCase().includes(normalized);
    const matchesCategory = selectedCategory === "ALL" || item.categoryName === selectedCategory;
    return matchesQuery && matchesCategory;
  });

  return (
    <div className="pos-menu-panel">
      <div className="pos-menu-filters">
        <input
          className="pos-search"
          placeholder="Search by dish name"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <select className="pos-category-filter" value={selectedCategory} onChange={(event) => setSelectedCategory(event.target.value)}>
          <option value="ALL">All categories</option>
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
      </div>
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
                <div className="pos-menu-card-badges">
                  {item.categoryName ? <span className="pos-menu-category-label">{item.categoryName}</span> : null}
                  <StatusPill tone={enabled ? "success" : "warning"}>{enabled ? "In stock" : "Unavailable"}</StatusPill>
                </div>
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
