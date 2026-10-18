 package repositories;

import models.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private Connection getConnection() throws SQLException {
        String url = System.getenv("PRODUCT_DB_URL");
        String user = System.getenv("PRODUCT_DB_USER");
        String password = System.getenv("PRODUCT_DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new SQLException(
                "Chua cau hinh PRODUCT_DB_URL, PRODUCT_DB_USER, PRODUCT_DB_PASSWORD"
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("quantity"),
            created == null ? null : created.toString(),
            updated == null ? null : updated.toString()
        );
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }

        return products;
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        }

        return null;
    }

    public Product create(
            String name,
            String description,
            BigDecimal price,
            int quantity) throws SQLException {

        String sql =
            "INSERT INTO products (name, description, price, quantity) " +
            "OUTPUT INSERTED.id " +
            "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, quantity);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return findById(rs.getInt(1));
                }
            }
        }

        return null;
    }

    public Product update(
            int id,
            String name,
            String description,
            BigDecimal price,
            int quantity) throws SQLException {

        String sql =
            "UPDATE products SET name = ?, description = ?, " +
            "price = ?, quantity = ?, updated_at = SYSDATETIME() " +
            "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, quantity);
            stmt.setInt(5, id);

            if (stmt.executeUpdate() == 0) {
                return null;
            }
        }

        return findById(id);
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}