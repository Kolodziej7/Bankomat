package com.atm.projectatm;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, Filters {
    private final DB_Connection db_connection = new DB_Connection();
    @FXML
    private TextField withdraw_tf, transfer_tf, transferId_tf, deposit_tf, oldPassword_tf, newPassword1_tf, newPassword2_tf;
    @FXML
    private Button deposit_bt, withdraw_bt, transfer_bt, logout_bt, changePassword_bt, delete_bt;
    @FXML
    private Label accountNumber_lbl, userInfo_lbl, balance_lbl;
    private static Double balanceToDouble, amountToChange, combinedBalance;
    private int newMaxLength;
    private int lengthChanger = 0;
    private Alert valueAlert;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        valueAlert = new Alert(Alert.AlertType.NONE);

        // FILTERS
        filter_2(deposit_tf,7);
        filter_2(withdraw_tf,7);
        filter_2(transfer_tf,7);
        filter_3(transferId_tf,6);
        filter_1(oldPassword_tf,15);
        filter_1(newPassword1_tf,15);
        filter_1(newPassword2_tf,15);


        // CHANGE PASSWORD BUTTON
        changePassword_bt.setOnAction(event -> {
            db_connection.changePassword(
                    event,
                    accountNumber_lbl.getText(),
                    oldPassword_tf.getText(),
                    newPassword1_tf.getText(),
                    newPassword2_tf.getText());

            oldPassword_tf.setText("");
            newPassword1_tf.setText("");
            newPassword2_tf.setText("");
        });

        // DEPOSIT BUTTON -> TRANSFER MONEY INTO YOUR ACCOUNT
        deposit_bt.setOnAction(event -> {
            if (deposit_tf.getText().equals("") || deposit_tf.getText() == null){
                valueAlert.setAlertType(Alert.AlertType.ERROR);
                valueAlert.setContentText("Empty field");
                valueAlert.showAndWait();
            } else {
                balanceToDouble = Double.parseDouble(balance_lbl.getText());
                amountToChange = Double.parseDouble(deposit_tf.getText());
                combinedBalance = Math.round((balanceToDouble+amountToChange)*100.0)/100.0;

                db_connection.balanceModification(
                        deposit_tf.getText(),
                        accountNumber_lbl.getText(),
                        balance_lbl.getText(),
                        true,
                        false,
                        false,
                        null);

                balance_lbl.setText(String.valueOf(combinedBalance));
                deposit_tf.setText("");
            }
        });

        // WITHDRAW BUTTON -> TAKE MONEY FROM YOUR ACCOUNT
        withdraw_bt.setOnAction(event -> {
            if (withdraw_tf.getText().equals("") || withdraw_tf.getText() == null){
                valueAlert.setAlertType(Alert.AlertType.ERROR);
                valueAlert.setContentText("Empty field");
                valueAlert.showAndWait();
            } else {
                balanceToDouble = Double.parseDouble(balance_lbl.getText());
                amountToChange = Double.parseDouble(withdraw_tf.getText());
                combinedBalance = Math.round((balanceToDouble-amountToChange)*100.0)/100.0;

                db_connection.balanceModification(
                        withdraw_tf.getText(),
                        accountNumber_lbl.getText(),
                        balance_lbl.getText(),
                        false,
                        true,
                        false,
                        null);
                if (db_connection.getChangeText()){
                    balance_lbl.setText(String.valueOf(combinedBalance));
                    db_connection.setChangeText(false);
                }
            }
            withdraw_tf.setText("");

        });

        // LOGOUT BUTTON -> BACK TO LOGIN.FXML
        logout_bt.setOnAction(event -> db_connection.changeScene(
                event,
                "Login.fxml",
                null,
                null,
                null,
                null));

        // TRANSFER BUTTON -> TRANSFER MONEY TO ANOTHER ACCOUNT
        transfer_bt.setOnAction(event -> {
                if (transfer_tf.getText() == null || transferId_tf.getText() == null || transfer_tf.getText().equals("") || transferId_tf.getText().equals("")){
                    valueAlert.setAlertType(Alert.AlertType.ERROR);
                    valueAlert.setContentText("Fields can't be empty");
                    valueAlert.showAndWait();
                } else {
                    balanceToDouble = Double.parseDouble(balance_lbl.getText());
                    amountToChange = Double.parseDouble(transfer_tf.getText());
                    combinedBalance = Math.round((balanceToDouble - amountToChange)*100.0)/100.0;
                    int transferId = Integer.parseInt(transferId_tf.getText());

                    db_connection.balanceModification(
                            transfer_tf.getText(),
                            accountNumber_lbl.getText(),
                            balance_lbl.getText(),
                            false,
                            false,
                            true,
                            transferId);
                    if (db_connection.getChangeText()){
                        balance_lbl.setText(String.valueOf(combinedBalance));
                        db_connection.setChangeText(false);
                    }
                }
            transfer_tf.setText("");
            transferId_tf.setText("");
        });

        // DELETE BUTTON -> DELETE YOUR ACCOUNT
        delete_bt.setOnAction(event -> db_connection.deleteAccount(event, accountNumber_lbl.getText()));
    }
    // FUNCTION TO SET USER INFO AFTER LOGGING IN
    protected void setBalance(String balance, String firstName, String lastName, String user_id){
        balance_lbl.setText(balance);
        userInfo_lbl.setText(firstName + " " + lastName);
        accountNumber_lbl.setText(user_id);
    }
    // FILTER -> SET LENGTH LIMIT
    @Override
    public void filter_1(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length()>maxLength){
                return null;
            }
            return change;
        }));
    }
    // FILTER -> SET LENGTH LIMIT & SECURITY
    @Override
    public void filter_2(TextField textField, int maxLength) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            // CHANGING LENGTH LIMIT
            if (lengthChanger == 0){
                // DEFAULT
                if (change.getControlText().contains(".")){
                    newMaxLength = change.getRangeEnd()+2;
                    lengthChanger = 2;
                }
                if (change.getControlNewText().length() > maxLength){
                    return null;
                }
                // AFTER DETECT '.'
            } else if (lengthChanger > 1) {
                if (!change.getControlText().contains(".")) {
                    newMaxLength = maxLength;
                    lengthChanger = 0;
                }
                if (change.getControlNewText().length() > newMaxLength){
                    return null;
                }
            }
            // USER CAN'T TYPE DOUBLE '0' BEFORE '.'
            if (change.getCaretPosition() <= 2 && !change.getText().matches("\\.") && change.getControlText().contains("0")){
                if (change.getText().matches("\\.")) {
                    return change;
                }
                change.setText("");
            }
            // USER CAN'T TYPE TWO '.'
            if (change.getControlText().contains(".") && change.getText().matches("\\.")){
                if (change.getText().matches("[0-9]+")){
                    return change;
                }
                change.setText("");
            }
            // '.' CAN'T BE TYPED FIRST
            if (change.getCaretPosition()<=1){
                if (change.getText().matches("[0-9]+")) {
                    return change;
                }else {
                    change.setText("");
                }
            }else if (change.getText().matches("[^0-9.]")) {
                change.setText("");
            }
            return change;
        }));
    }
    // FILTER -> SET LENGTH LIMIT & ACCEPT ONLY NUMBERS
    @Override
    public void filter_3(TextField textField, int maxLength) {
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
}

