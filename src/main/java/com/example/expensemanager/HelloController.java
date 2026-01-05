package com.example.expensemanager;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class HelloController {

    private static final String FILE_NAME = "expenses.txt";

    private Expense selectedExpenseForEdit = null;

    // ===== UI FIELDS =====
    @FXML private TextField amountField;
    @FXML private TextField categoryField;
    @FXML private TextField searchField;

    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;

    @FXML private Label totalLabel;
    @FXML private Button themeButton;

    // ===== THEME =====
    private boolean darkMode = false;

    @FXML
    private void toggleDarkMode() {
        Scene scene = themeButton.getScene();
        scene.getStylesheets().clear();

        if (!darkMode) {
            scene.getStylesheets().add(
                    getClass().getResource("dark.css").toExternalForm()
            );
            themeButton.setText("☀ Light Mode");
            darkMode = true;
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("style.css").toExternalForm()
            );
            themeButton.setText("🌙 Dark Mode");
            darkMode = false;
        }
    }

    // ===== DATA =====
    private ObservableList<Expense> expenseList =
            FXCollections.observableArrayList();

    private FilteredList<Expense> filteredExpenses =
            new FilteredList<>(expenseList, p -> true);

    // ===== INITIALIZE =====
    @FXML
    public void initialize() {

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );
        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        expenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        expenseTable.setItems(filteredExpenses);

        // Search logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String search = newVal.toLowerCase();

            filteredExpenses.setPredicate(expense ->
                    search.isEmpty() ||
                            expense.getCategory().toLowerCase().contains(search)
            );

            updateTotalFiltered();
        });

        loadExpensesFromFile();
        updateTotalFiltered();
    }

    // ===== ADD =====
    @FXML
    private void handleAddExpense() {

        String amountText = amountField.getText();
        String category = categoryField.getText();

        if (amountText.isEmpty() || category.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all fields");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Amount must be greater than 0");
                return;
            }

            expenseList.add(new Expense(amount, category));
            saveExpensesToFile();
            updateTotalFiltered();

            amountField.clear();
            categoryField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid number");
        }
    }

    // ===== DELETE =====
    @FXML
    private void handleDeleteExpense() {

        Expense selected =
                expenseTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Select an expense to delete");
            return;
        }

        expenseList.remove(selected);
        saveExpensesToFile();
        updateTotalFiltered();
    }

    // ===== SELECT =====
    @FXML
    private void handleSelectExpense() {

        selectedExpenseForEdit =
                expenseTable.getSelectionModel().getSelectedItem();

        if (selectedExpenseForEdit != null) {
            amountField.setText(
                    String.valueOf(selectedExpenseForEdit.getAmount())
            );
            categoryField.setText(
                    selectedExpenseForEdit.getCategory()
            );
        }
    }

    // ===== UPDATE =====
    @FXML
    private void handleUpdateExpense() {

        if (selectedExpenseForEdit == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Select an expense to update");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryField.getText();

            if (amount <= 0 || category.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter valid values");
                return;
            }

            selectedExpenseForEdit.setAmount(amount);
            selectedExpenseForEdit.setCategory(category);

            expenseTable.refresh();
            saveExpensesToFile();
            updateTotalFiltered();

            amountField.clear();
            categoryField.clear();
            selectedExpenseForEdit = null;

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid number");
        }
    }

    // ===== TOTAL (SINGLE SOURCE OF TRUTH) =====
    private void updateTotalFiltered() {
        double total = 0;
        for (Expense e : filteredExpenses) {
            total += e.getAmount();
        }
        totalLabel.setText("Total Expense: ₹" + total);
    }

    // ===== FILE =====
    private void saveExpensesToFile() {
        try (java.io.PrintWriter writer =
                     new java.io.PrintWriter(new java.io.FileWriter(FILE_NAME))) {

            for (Expense e : expenseList) {
                writer.println(e.getAmount() + "," + e.getCategory());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not save file");
        }
    }

    private void loadExpensesFromFile() {
        java.io.File file = new java.io.File(FILE_NAME);
        if (!file.exists()) return;

        try (java.util.Scanner sc = new java.util.Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                expenseList.add(
                        new Expense(
                                Double.parseDouble(parts[0]),
                                parts[1]
                        )
                );
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not load file");
        }
    }

    // ===== ALERT =====
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
