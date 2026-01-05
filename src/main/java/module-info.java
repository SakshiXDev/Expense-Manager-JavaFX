module com.example.expensemanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.expensemanager to javafx.fxml;
    exports com.example.expensemanager;
}