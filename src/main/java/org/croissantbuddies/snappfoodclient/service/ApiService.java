package org.croissantbuddies.snappfoodclient.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApiService {

    private static final String API_BASE_URL = "http://localhost:8000";
    private final HttpClient client = HttpClient.newHttpClient();

    public String login(String phone, String password) throws IOException, InterruptedException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("phone", phone);
        jsonRequest.put("password", password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.getString("error"));
        }

        return response.body();
    }
    public String register(String fullName, String phone, String password, String address, String role) throws IOException, InterruptedException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("full_name", fullName);
        jsonRequest.put("phone", phone);
        jsonRequest.put("password", password);
        jsonRequest.put("address", address);
        jsonRequest.put("role", role.toLowerCase());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("error", "An unknown error occurred."));
        }
        return response.body();
    }
    public String getUsers(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/users"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("error", "Failed to fetch users."));
        }

        return response.body();
    }
    public void updateUserStatus(String token, long userId, String status) throws IOException, InterruptedException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("status", status);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/users/" + userId + "/status"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("error", "Failed to update status."));
        }
    }
    public String getOrders(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/orders"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String responseBody = response.body();
            String errorMessage = "Failed to fetch orders.";
            if (responseBody != null && !responseBody.isEmpty() && responseBody.trim().startsWith("{")) {
                JSONObject errorJson = new JSONObject(responseBody);
                errorMessage = errorJson.optString("error", errorMessage);
            }
            throw new IOException(errorMessage);
        }
        return response.body();
    }

    public String getCoupons(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/coupons"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to fetch coupons: " + response.body());
        return response.body();
    }

    public String createCoupon(String token, JSONObject couponData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/coupons"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(couponData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new IOException("Failed to create coupon: " + response.body());
        return response.body();
    }

    public String updateCoupon(String token, long couponId, JSONObject couponData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/coupons/" + couponId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(couponData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to update coupon: " + response.body());
        return response.body();
    }

    public void deleteCoupon(String token, long couponId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/coupons/" + couponId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to delete coupon: " + response.body());
    }
    public String getTransactions(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/transactions"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch transactions: " + response.body());
        }

        return response.body();
    }

    public String getMyRestaurants(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/mine"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to fetch restaurants: " + response.body());
        return response.body();
    }

    public void createRestaurant(String token, JSONObject restaurantData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(restaurantData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IOException("Failed to create restaurant: " + response.body());
        }
    }
    public String getProfile(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/auth/profile"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to fetch profile: " + response.body());
        return response.body();
    }

    public void updateProfile(String token, JSONObject profileData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/auth/profile"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(profileData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Failed to update profile: " + response.body());
    }
    public void updateRestaurant(String token, long restaurantId, JSONObject restaurantData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(restaurantData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to update restaurant: " + response.body());
        }
    }
    public void addFoodItem(String token, long restaurantId, JSONObject foodData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/item"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(foodData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) { // 201 Created
            throw new IOException("Failed to add food item: " + response.body());
        }
    }

    public String getRestaurantDetails(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()

                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/details"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch restaurant details: " + response.body());
        }
        return response.body();
    }
    public void updateFoodItem(String token, long restaurantId, long foodId, JSONObject foodData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/item/" + foodId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(foodData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to update food item: " + response.body());
        }
    }

    public void deleteFoodItem(String token, long restaurantId, long foodId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/item/" + foodId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to delete food item: " + response.body());
        }
    }
    public void addMenu(String token, long restaurantId, JSONObject menuData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(menuData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IOException("Failed to add menu: " + response.body());
        }
    }
    public String getMenusForRestaurant(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch menus: " + response.body());
        }
        return response.body();
    }
    public void deleteMenu(String token, long restaurantId, long menuId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu/" + menuId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 204) { // 200 OK or 204 No Content
            throw new IOException("Failed to delete menu: " + response.body());
        }
    }

    public void addFoodToMenu(String token, long restaurantId, long menuId, long foodId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu/" + menuId + "/food/" + foodId))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to add food to menu: " + response.body());
        }
    }
    public void removeFoodFromMenu(String token, long restaurantId, long menuId, long foodId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu/" + menuId + "/food/" + foodId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to remove food from menu: " + response.body());
        }
    }
    public String getFoodsInMenu(String token, long restaurantId, long menuId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/menu/" + menuId + "/foods"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to get foods in menu: " + response.body());
        }
        return response.body();
    }
    public String getSellerOrders(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/orders/mine"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch seller orders: " + response.body());
        }
        return response.body();
    }
    public String getAllVendors(String token) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/vendors"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch vendors: " + response.body());
        }
        return response.body();
    }
    public String getVendorDetails(String token, long vendorId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/vendors/" + vendorId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch vendor details: " + response.body());
        }
        return response.body();
    }
    public void addToCart(String token, long foodId, int quantity) throws IOException, InterruptedException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("foodId", foodId);
        jsonRequest.put("quantity", quantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/cart/add"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to add to cart: " + response.body());
        }
    }

    public String getCart(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/cart"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to get cart: " + response.body());
        }
        return response.body();
    }
    public String getOrderHistory(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/orders/history"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch order history: " + response.body());
        }
        return response.body();
    }
    public void removeFromCart(String token, long foodId) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("foodId", foodId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/cart/remove"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {

            try {
                JSONObject errorJson = new JSONObject(response.body());
                throw new IOException("Failed to remove item: " + errorJson.getString("error"));
            } catch (Exception e) {
                throw new IOException("Failed to remove item: " + response.body());
            }
        }
    }
    public JSONObject topUpWallet(String token, double amount) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("amount", amount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/wallet/top-up"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            try {
                JSONObject errorJson = new JSONObject(response.body());
                throw new IOException(errorJson.optString("error", "Failed to top up wallet."));
            } catch (Exception e) {
                throw new IOException("Failed to top up wallet: " + response.body());
            }
        }
        return new JSONObject(response.body());
    }

    public void updateOrderStatus(String token, long orderId, String newStatus) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("status", newStatus);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/orders/" + orderId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            try {
                JSONObject errorJson = new JSONObject(response.body());
                throw new IOException(errorJson.optString("error", "Failed to update order status."));
            } catch (Exception e) {
                throw new IOException("Failed to update order status: " + response.body());
            }
        }
    }
    public String getAvailableDeliveries(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/deliveries/available"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch available deliveries: " + response.body());
        }
        return response.body();
    }
    public String getDeliveryHistory(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/deliveries/history"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch delivery history: " + response.body());
        }
        return response.body();
    }
    public void updateDeliveryStatus(String token, long orderId, String newStatus) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("status", newStatus);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/deliveries/" + orderId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to update delivery status: " + response.body());
        }
    }
    public String checkCoupon(String token, String couponCode, long vendorId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/coupons?coupon_code=" + couponCode + "&vendor_id=" + vendorId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to check coupon: " + response.body());
        }
        return response.body();
    }
    public String submitOrder(String token, String orderData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/orders/submit"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to submit order: " + response.body());
        }
        return response.body();
    }
    public void submitRating(String token, long orderId, int rating, String comment, List<String> imageBase64) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("order_id", orderId);
        requestBody.put("rating", rating);
        requestBody.put("comment", comment);
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            requestBody.put("imageBase64", new JSONArray(imageBase64));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/ratings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to submit rating: " + response.body());
        }
    }
    public List<Long> getRatedOrderIds(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/ratings/ids"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.body());
        for (int i = 0; i < jsonArray.length(); i++) {
            ids.add(jsonArray.getLong(i));
        }
        return ids;
    }
    public String getRestaurantRatings(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/vendors/" + restaurantId + "/ratings"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch ratings: " + response.body());
        }
        return response.body();
    }

    public String getFavoriteRestaurants(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/favorites"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch favorites: " + response.body());
        }
        return response.body();
    }

    public void addToFavorites(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/favorites/" + restaurantId))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to add to favorites: " + response.body());
        }
    }

    public void removeFromFavorites(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/favorites/" + restaurantId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to remove from favorites: " + response.body());
        }
    }
    public String getSellerOrderDetails(String token, long orderId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/orders/" + orderId + "/details"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch order details: " + response.body());
        }
        return response.body();
    }
    public String getRestaurantCoupons(String token, long restaurantId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/coupons"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch restaurant coupons: " + response.body());
        }
        return response.body();
    }

    public void createRestaurantCoupon(String token, long restaurantId, JSONObject couponData) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/restaurants/" + restaurantId + "/coupons"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(couponData.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IOException("Failed to create coupon: " + response.body());
        }
    }
    public String searchFoodItems(String token, String search, Integer minPrice, Integer maxPrice, List<String> keywords) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("search", search);
        if (minPrice != null) {
            requestBody.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            requestBody.put("price", maxPrice);
        }
        requestBody.put("keywords", new JSONArray(keywords));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/items"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to search for food items: " + response.body());
        }
        return response.body();
    }
    public String getOrderHistory(String token, String status, LocalDate startDate, LocalDate endDate) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL + "/buyers/orders/history");
        List<String> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            params.add("status=" + status);
        }
        if (startDate != null) {
            params.add("start_date=" + startDate.toString());
        }
        if (endDate != null) {
            params.add("end_date=" + endDate.toString());
        }

        if (!params.isEmpty()) {
            urlBuilder.append("?").append(String.join("&", params));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch order history: " + response.body());
        }
        return response.body();
    }
    public String getOrderDetails(String token, long orderId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/buyers/orders/" + orderId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch order details: " + response.body());
        }
        return response.body();
    }
    public String getAdminOrderDetails(String token, long orderId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/admin/orders/" + orderId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch order details: " + response.body());
        }
        return response.body();
    }
}