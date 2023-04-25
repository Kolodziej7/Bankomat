package com.atm.projectatm;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;
public class LoginController implements Initializable, Filters {
    private final DB_Connection db_connection = new DB_Connection();
    @FXML
    private TextField login_tf;
    @FXML
    private PasswordField password_tf;
    @FXML
    private Button login_bt, register_bt, exit_bt;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // FILTERS
        filter_2(login_tf, 9);
        filter_1(password_tf,15);

        // ENABLE LOGIN BY CLICKING ENTER
        password_tf.setOnAction(event -> {
            password_tf.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().equals(KeyCode.ENTER)){
                    db_connection.logIn(event, login_tf.getText(), password_tf.getText());
                }
            });
        });

        // LOGIN BUTTON -> LOG INTO THE SYSTEM
        login_bt.setOnAction(event -> db_connection.logIn(
                event,
                login_tf.getText(),
                password_tf.getText()));

        // REGISTER BUTTON -> CHANGE SCENE TO REGISTER.FXML
        register_bt.setOnAction(event -> db_connection.changeScene(
                event,
                "Register.fxml",
                null,
                null,
                null,
                null));

        // EXIT BUTTON -> CLOSE THE APPLICATION
        exit_bt.setOnAction(event -> System.exit(0));
    }
    // FILTER -> SET LENGTH LIMIT & PASSWORD REGEX
    @Override
    public void filter_1(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length()>maxLength){
                return null;
            }
            if (change.getText().matches("[a-zA-Z0-9_!@#$%&]")){
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


    @Override
    public void filter_3(TextField textField, int maxLength) {}
}
