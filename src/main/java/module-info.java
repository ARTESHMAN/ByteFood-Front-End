module org.croissantbuddies.snappfoodclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;

    requires com.google.gson;
    requires java.desktop;

    opens org.croissantbuddies.snappfoodclient to javafx.fxml;
    exports org.croissantbuddies.snappfoodclient;

    opens org.croissantbuddies.snappfoodclient.controller to javafx.fxml;
    exports org.croissantbuddies.snappfoodclient.controller;

    opens org.croissantbuddies.snappfoodclient.model to javafx.fxml;
    exports org.croissantbuddies.snappfoodclient.model;

    opens org.croissantbuddies.snappfoodclient.manager to javafx.fxml;
    exports org.croissantbuddies.snappfoodclient.manager;
}