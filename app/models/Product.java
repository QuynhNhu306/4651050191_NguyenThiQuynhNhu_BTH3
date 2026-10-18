package models;

import java.math.BigDecimal;

public class Product {

    public int id;
    public String name;
    public String description;
    public BigDecimal price;
    public int quantity;
    public String createdAt;
    public String updatedAt;

    public Product() {
    }

    public Product(
            int id,
            String name,
            String description,
            BigDecimal price,
            int quantity,
            String createdAt,
            String updatedAt) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}