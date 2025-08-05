package org.croissantbuddies.snappfoodclient.manager;

public class SessionManager {
    private static String authToken;
    private static Long userId;
    private static String userFullName;
    private static String userRole;
    private static String profileImageBase64;
    private static String userStatus;

    public static void login(String token, Long userId, String fullName, String role, String profileImage, String status) { // <-- پارامتر status اضافه شود
        authToken = token;
        SessionManager.userId = userId;
        userFullName = fullName;
        userRole = role;
        profileImageBase64 = profileImage;
        userStatus = status;
    }

    public static void logout() {
        authToken = null;
        userId = null;
        userFullName = null;
        userRole = null;
        profileImageBase64 = null;
        userStatus = null;
    }

    public static String getAuthToken() { return authToken; }
    public static Long getUserId() { return userId; }
    public static String getUserFullName() { return userFullName; }
    public static String getUserRole() { return userRole; }
    public static String getProfileImageBase64() { return profileImageBase64; }
    public static String getUserStatus() { return userStatus; }
    public static void updateName(String newFullName) { userFullName = newFullName; }
    public static boolean isLoggedIn() { return authToken != null; }
}