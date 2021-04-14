module by.bsuir {
    requires javafx.controls;
    requires javafx.fxml;

    opens by.bsuir to javafx.fxml;
    exports by.bsuir;
}