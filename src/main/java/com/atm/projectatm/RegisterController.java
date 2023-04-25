package com.atm.projectatm;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable, Filters {
    private final DB_Connection db_connection = new DB_Connection();
    @FXML
    private TextField firstName_tf, lastName_tf, password_tf, phoneNumber_tf;
    @FXML
    private Button save_bt, back_bt;
    @FXML
    private RadioButton male_rb, female_rb, selected_rb;
    @FXML
    private DatePicker date_pc;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // RADIOBUTTONS
        ToggleGroup toggleGroup = new ToggleGroup();
        male_rb.setToggleGroup(toggleGroup);
        female_rb.setToggleGroup(toggleGroup);
        male_rb.setSelected(true);
        Alert textfieldAlert = new Alert(Alert.AlertType.NONE);

        // FILTERS
        filter_1(firstName_tf, 15);
        filter_1(lastName_tf, 15);
        filter_3(password_tf, 15);
        filter_2(phoneNumber_tf,9);

        // SAVE BUTTON -> ADD NEW USER
        save_bt.setOnAction(event -> {
            if (firstName_tf.getText().equals("") || lastName_tf.getText().equals("") || password_tf.getText().equals("")){
                textfieldAlert.setAlertType(Alert.AlertType.WARNING);
                textfieldAlert.setContentText("Some fields are empty or not filled correctly");
                textfieldAlert.showAndWait();
            } else {
                selected_rb = (RadioButton) toggleGroup.getSelectedToggle();

                db_connection.signUp(event,
                        db_connection.validateId(),
                        firstName_tf.getText(),
                        lastName_tf.getText(),
                        password_tf.getText(),
                        date_pc.getValue(),
                        phoneNumber_tf.getText(),
                        selected_rb.getText());
            }
        });

        // CANCEL BUTTON -> BACK TO LOGIN.FXML
        back_bt.setOnAction(event -> db_connection.changeScene(
                event,
                "Login.fxml",
                null,
                null,
                null,
                null));
    }
    // FILTER -> SET LENGTH LIMIT & ACCEPT ONLY LETTERS
    @Override
    public void filter_1(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() > maxLength){
                return null;
            }
            //if (change.getText().matches("[a-zA-Z]+")) {
            if (change.getText().matches("\\p{IsAlphabetic}")) {
                return change;
            } else {
                change.setText("");
            }
            return change;
        }));
    }
    // FILTER -> SET LENGTH LIMIT & ACCEPT ONLY NUMBERS
    @Override
    public void filter_2(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() > maxLength){
                return null;
            }
            if (change.getText().matches("\\d+")) {
                return change;
            } else {
                change.setText("");
            }
            return change;
        }));
    }

    // FILTER -> SET LENGTH LIMIT & PASSWORD REGEX
    @Override
    public void filter_3(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length()>maxLength){
                return null;
            }
            if (change.getText().matches("[a-zA-Z0-9_!@#$%&]")){
                return change;
            } else{
                change.setText("");
            }
            return change;
        }));
    }
}
