package com.restaurant.kitchen.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "kitchen_ticket_items")
public class KitchenTicketItemEntity {

    @Id
    @Column(name = "ticket_item_id", nullable = false, length = 64)
    private String ticketItemId;

    @Column(name = "ticket_id", nullable = false, length = 64)
    private String ticketId;

    @Column(name = "order_item_id", nullable = false, length = 64)
    private String orderItemId;

    @Column(name = "menu_item_id", nullable = false, length = 64)
    private String menuItemId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "prep_status", nullable = false, length = 32)
    private String prepStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    protected KitchenTicketItemEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getTicketItemId() {
        return ticketItemId;
    }

    public void setTicketItemId(String ticketItemId) {
        this.ticketItemId = ticketItemId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPrepStatus() {
        return prepStatus;
    }

    public void setPrepStatus(String prepStatus) {
        this.prepStatus = prepStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static KitchenTicketItemEntity create(String ticketItemId,
                                                 String ticketId,
                                                 String orderItemId,
                                                 String menuItemId,
                                                 String itemName,
                                                 Integer quantity,
                                                 String prepStatus) {
        KitchenTicketItemEntity entity = new KitchenTicketItemEntity();
        entity.setTicketItemId(ticketItemId);
        entity.setTicketId(ticketId);
        entity.setOrderItemId(orderItemId);
        entity.setMenuItemId(menuItemId);
        entity.setItemName(itemName);
        entity.setQuantity(quantity);
        entity.setPrepStatus(prepStatus);
        return entity;
    }
}
