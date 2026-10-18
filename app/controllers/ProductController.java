package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ProductRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionException;

public class ProductController extends Controller {

    private final ProductRepository repository;
    private final HttpClient httpClient;

    @Inject
    public ProductController() {
        this.repository = new ProductRepository();
        this.httpClient = HttpClient.newHttpClient();
    }

    // Kiem tra token bang Authentication Service cua Bai 2
    private boolean isAuthenticated(Http.Request request) {
        String authorization = request.getHeaders()
                .get("Authorization")
                .orElse("");

        if (!authorization.startsWith("Bearer ")) {
            return false;
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            return false;
        }

        String authUrl = System.getenv("PRODUCT_AUTH_VALIDATE_URL");

        if (authUrl == null || authUrl.isBlank()) {
            throw new IllegalStateException(
                "Chua cau hinh PRODUCT_AUTH_VALIDATE_URL"
            );
        }

        try {
            HttpRequest authRequest = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    authRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() >= 200
                    && response.statusCode() < 300;

        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    private Result authenticationError(Http.Request request) {
        try {
            if (!isAuthenticated(request)) {
    return unauthorizedResponse();
}
            return null;
        } catch (Exception e) {
            return status(503, Json.newObject()
                    .put("error", "Authentication service unavailable"));
        }
    }

    private Result unauthorizedResponse() {
    return status(401, Json.newObject()
            .put("error", "Unauthorized: token khong hop le"));
}

    // GET /products
    public Result getAll(Http.Request request) {
        Result error = authenticationError(request);
        if (error != null) return error;

        try {
            return ok(Json.toJson(repository.findAll()));
        } catch (Exception e) {
            return internalServerError(Json.newObject()
                    .put("error", "Khong the lay danh sach san pham"));
        }
    }

    // GET /products/:id
    public Result getById(Http.Request request, int id) {
        Result error = authenticationError(request);
        if (error != null) return error;

        try {
            var product = repository.findById(id);

            if (product == null) {
                return notFound(Json.newObject()
                        .put("error", "Khong tim thay san pham"));
            }

            return ok(Json.toJson(product));

        } catch (Exception e) {
            return internalServerError(Json.newObject()
                    .put("error", "Loi truy van san pham"));
        }
    }

    // POST /products
    public Result create(Http.Request request) {
        Result error = authenticationError(request);
        if (error != null) return error;

        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest(Json.newObject()
                    .put("error", "Body phai la JSON"));
        }

        try {
            String name = requiredText(json, "name");
            String description = optionalText(json, "description");
            BigDecimal price = requiredPrice(json, "price");
            int quantity = requiredQuantity(json, "quantity");

            if (name.isBlank() || price.signum() < 0 || quantity < 0) {
                return badRequest(Json.newObject()
                        .put("error", "Du lieu san pham khong hop le"));
            }

            var product = repository.create(
                    name, description, price, quantity);

            return created(Json.toJson(product));

        } catch (IllegalArgumentException e) {
            return badRequest(Json.newObject().put("error", e.getMessage()));
        } catch (Exception e) {
            return internalServerError(Json.newObject()
                    .put("error", "Khong the tao san pham"));
        }
    }

    // PUT /products/:id
    public Result update(Http.Request request, int id) {
        Result error = authenticationError(request);
        if (error != null) return error;

        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest(Json.newObject()
                    .put("error", "Body phai la JSON"));
        }

        try {
            String name = requiredText(json, "name");
            String description = optionalText(json, "description");
            BigDecimal price = requiredPrice(json, "price");
            int quantity = requiredQuantity(json, "quantity");

            if (name.isBlank() || price.signum() < 0 || quantity < 0) {
                return badRequest(Json.newObject()
                        .put("error", "Du lieu san pham khong hop le"));
            }

            var product = repository.update(
                    id, name, description, price, quantity);

            if (product == null) {
                return notFound(Json.newObject()
                        .put("error", "Khong tim thay san pham"));
            }

            return ok(Json.toJson(product));

        } catch (IllegalArgumentException e) {
            return badRequest(Json.newObject().put("error", e.getMessage()));
        } catch (Exception e) {
            return internalServerError(Json.newObject()
                    .put("error", "Khong the cap nhat san pham"));
        }
    }

    // DELETE /products/:id
    public Result delete(Http.Request request, int id) {
        Result error = authenticationError(request);
        if (error != null) return error;

        try {
            if (!repository.delete(id)) {
                return notFound(Json.newObject()
                        .put("error", "Khong tim thay san pham"));
            }

            return ok(Json.newObject()
                    .put("message", "Xoa san pham thanh cong"));

        } catch (Exception e) {
            return internalServerError(Json.newObject()
                    .put("error", "Khong the xoa san pham"));
        }
    }

    private String requiredText(JsonNode json, String field) {
        JsonNode value = json.get(field);

        if (value == null || !value.isTextual()) {
            throw new IllegalArgumentException(
                    field + " la truong bat buoc dang chuoi");
        }

        return value.asText();
    }

    private String optionalText(JsonNode json, String field) {
        JsonNode value = json.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        if (!value.isTextual()) {
            throw new IllegalArgumentException(
                    field + " phai la chuoi");
        }

        return value.asText();
    }

    private BigDecimal requiredPrice(JsonNode json, String field) {
        JsonNode value = json.get(field);

        if (value == null || !value.isNumber()) {
            throw new IllegalArgumentException(
                    field + " phai la so");
        }

        return value.decimalValue();
    }

    private int requiredQuantity(JsonNode json, String field) {
        JsonNode value = json.get(field);

        if (value == null || !value.isIntegralNumber()
                || !value.canConvertToInt()) {
            throw new IllegalArgumentException(
                    field + " phai la so nguyen");
        }

        return value.intValue();
    }
}