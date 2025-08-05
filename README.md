# 🍽️ ByteFood Clone - Full-Stack Food Delivery Platform

A comprehensive, full-stack food delivery application inspired by platforms like **Snappfood**. This project features a complete ecosystem with four distinct user roles, a robust Java-based backend with Hibernate, and a modern JavaFX desktop client.

---

## 📖 About The Project

This application simulates a real-world food ordering service, providing a feature-rich experience for:
- 👤 Customers (Buyers)
- 👨‍🍳 Restaurant Owners (Sellers)
- 🛵 Couriers
- ⚙️ Administrators

It's built from the ground up using **Core Java technologies**, offering a deep dive into backend development and desktop UI design.

---

## ✨ Key Features

### 👤 Buyer
- **Browse & Search**: Search restaurants and food items by name, keywords, or price range.
- **Order Management**: Add items to cart, apply coupons, and place orders.
- **History & Ratings**: View order history, details, and submit ratings with comments/images.
- **Favorites**: Mark and view favorite restaurants.

### 👨‍🍳 Seller
- **Restaurant Management**: Update name, address, and logo.
- **Menu & Food Management**: Full CRUD on food items with pricing, keywords, and images.
- **Order Processing**: View and update status of incoming orders.
- **Promotions**: Create/manage discount coupons.

### 🛵 Courier
- **Delivery Management**: View available orders for pickup.
- **My Deliveries**: Accept jobs and view delivery history.

### ⚙️ Admin
- **User Management**: Approve/reject new Sellers and Couriers.
- **Order Oversight**: Monitor all platform orders.
- **Transaction Monitoring**: View complete transaction history.
- **Global Promotions**: Create universal discount codes.

---

## 🛠️ Tech Stack

### 🔧 Backend
- **Java 17**
- **Custom HTTP Server** (`com.sun.net.httpserver`)
- **MySQL + Hibernate (ORM)**
- **JWT Authentication**
- **Gson / org.json** for JSON handling

### 🎨 Frontend
- **JavaFX** (Desktop UI)
- **FXML** for layout
- **CSS** for styling

### ⚙️ Build Tool
- **Apache Maven**

---

## 🚀 Getting Started

### 📦 Prerequisites
- Java JDK 17+
- Apache Maven
- MySQL Server

---

### 🔙 Backend Setup
1. **Clone Backend Repo**
   ```bash
   git clone https://github.com/ARTESHMAN/Byte-Food-Backend.git
   ```

2. **Configure MySQL**
   - Create a `snappfood` schema in MySQL
   - Update DB credentials in `src/main/resources/hibernate.cfg.xml`
   - Keep `hbm2ddl.auto` = `update`

3. **Run the Backend Server**
   - Run:
     ```java
     org.croissantbuddies.snappfood.main.MainServer.java
     ```
   - Server available at: `http://localhost:8000`

---

### 🖥️ Frontend Setup

1. **Clone Frontend Repo**
   ```bash
   git clone https://github.com/ARTESHMAN/ByteFood-Front-End.git
   ```

2. **Run JavaFX Application**
   - Run:
     ```java
     org.croissantbuddies.snappfoodclient.HelloApplication.java
     ```
   - The UI will launch — you're ready to explore the full app!

---

## 📫 Contact

Have feedback or want to collaborate?  
📧 alimoghaddam5966@gmail.com  
🔗 GitHub: [@ARTESHMAN](https://github.com/ARTESHMAN)
