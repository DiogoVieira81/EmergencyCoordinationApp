package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import enums.OperationType;

public class OperationsController {
    @FXML private ListView<String> operationList;
    @FXML private TextField operationNameInput;
    @FXML private ComboBox<OperationType> operationTypeCombo;

    @FXML
    private void initiateOperation() {
        String operationName = operationNameInput.getText();
        OperationType operationType = operationTypeCombo.getValue();
        // Implement logic to initiate new operation
        operationNameInput.clear();
    }

    public void initialize() {
        // Load existing operations and populate operationTypeCombo
        operationTypeCombo.getItems().addAll(OperationType.values());
    }
}
