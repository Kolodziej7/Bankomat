package com.atm.projectatm;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

public class DB_Connection{
    private boolean changeText = false;
    private int range;
    public void setChangeText(boolean changeText) {
        this.changeText = changeText;
    }
    public boolean getChangeText() {
        return changeText;
    }
    // FUNCTION TO JUMP BETWEEN SCENES
    protected void changeScene(ActionEvent event, String fxmlFile, String balance, String firstName, String lastName, String user_id){
        Parent root = null;
        FXMLLoader loader;
        MainController mainController;
        Stage stage;

        if (balance != null) {
            try {
                loader = new FXMLLoader(DB_Connection.class.getResource(fxmlFile));
                root = loader.load();
                mainController = loader.getController();
                mainController.setBalance(balance, firstName, lastName, user_id);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            try {
                root = FXMLLoader.load(Objects.requireNonNull(DB_Connection.class.getResource(fxmlFile)));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        assert root != null;
        stage.setScene(new Scene(root, 600,400));
        stage.show();
    }
    // SIGN UP FUNCTION -> ADD NEW USER DATA TO DATABASE
    protected void signUp(ActionEvent event, String user_id, String firstName, String lastName, String password, LocalDate birthDate, String phone_number, String gender){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        ResultSet resultSet = null;
        Alert signUpAlert = new Alert(Alert.AlertType.NONE);
        LocalDateTime currentDate = LocalDateTime.now();
        int age;
        // CONNECT TO DATABASE
        try {
            // ACCESS DATABASE
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
            // MYSQL SERVER
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
            preparedStatement1 = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            preparedStatement1.setString(1,  user_id);
            resultSet = preparedStatement1.executeQuery();
            age = Period.between(birthDate, LocalDate.from(currentDate)).getYears();
            // ERRORS
            if (resultSet.isBeforeFirst()){
                throw new AlertException("ID already exist in system, please try again");
            } else if (age<18) {
                throw new AlertException("You must have at least 18 yo to create account");
            } else if (age > 100){
                throw new AlertException("Insert your true age");
            } else if (phone_number.length() < 9) {
                throw new AlertException("Please enter full phone number");
                // SAVE USER DATA
            } else {
                preparedStatement = connection.prepareStatement("INSERT INTO users (user_id, firstName, lastName, age, password, dateOfBirth, phone_number, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, user_id);
                preparedStatement.setString(2, firstName);
                preparedStatement.setString(3, lastName);
                preparedStatement.setInt(4, Period.between(birthDate, LocalDate.from(currentDate)).getYears());
                preparedStatement.setString(5, password);
                preparedStatement.setDate(6, Date.valueOf(birthDate));
                preparedStatement.setString(7, phone_number);
                preparedStatement.setString(8, gender);
                preparedStatement.executeUpdate();
                // SUCCESS ALERT
                signUpAlert.setAlertType(Alert.AlertType.INFORMATION);
                signUpAlert.setTitle("SYSTEM INFORMATION");
                signUpAlert.setHeaderText("Congratulations! Your account has been created");
                signUpAlert.setContentText("Your account Id: " + user_id);
                signUpAlert.showAndWait();
                // CHANGE SCENE -> BACK INTO LOGIN PAGE
                changeScene(event, "Login.fxml", null, null, null, null);
            }
        } catch (AlertException exception){
            signUpAlert.setAlertType(Alert.AlertType.ERROR);
            signUpAlert.setHeaderText("SYSTEM INFORMATION");
            signUpAlert.setContentText(exception.getMessage());
            signUpAlert.showAndWait();
        } catch (NullPointerException exception){
            signUpAlert.setAlertType(Alert.AlertType.WARNING);
            signUpAlert.setHeaderText("SYSTEM INFORMATION");
            signUpAlert.setContentText("Some fields are empty or not filled correctly");
            signUpAlert.show();
        } catch (SQLException exception){
            exception.printStackTrace();
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
        // CLOSE ALL CONNECTIONS
        finally {
            if (resultSet != null){
                try{
                    resultSet.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement1 != null){
                try{
                    preparedStatement1.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement!= null){
                try{
                    preparedStatement.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try{
                    connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
    // LOG IN FUNCTION -> IF USER HAVE AN ACCOUNT THEN HE CAN LOG IN
    public void logIn(ActionEvent event, String user_id, String password){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Alert logInAlert = new Alert(Alert.AlertType.NONE);
        // CONNECT TO DATABASE
        try{
            // ACCESS DATABASE
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
            // MYSQL SERVER
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
            preparedStatement = connection.prepareStatement("SELECT password, balance, firstName, lastName FROM users WHERE user_id = ?");
            preparedStatement.setString(1, String.valueOf(user_id));
            resultSet = preparedStatement.executeQuery();
            // LOOKING FOR ACCOUNT
            if (!resultSet.isBeforeFirst()){
                throw new AlertException("Account doesn't exist in the system");
            } else {
                while (resultSet.next()){
                    String receivedPassword = resultSet.getString("password");
                    String accountBalance = resultSet.getString("balance");
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");
                    // CHECKING PASSWORD
                    if (receivedPassword.equals(password)){
                        changeScene(event, "Main.fxml", accountBalance, firstName, lastName, user_id);
                    } else {
                        throw new AlertException("Wrong password");
                    }
                }
            }
        } catch (AlertException exception){
            logInAlert.setAlertType(Alert.AlertType.ERROR);
            logInAlert.setHeaderText("SYSTEM INFORMATION");
            logInAlert.setContentText(exception.getMessage());
            logInAlert.showAndWait();
        } catch (SQLException exception){
            exception.printStackTrace();
            // CLOSE ALL CONNECTIONS
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null){
                try{
                    resultSet.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement != null){
                try{
                    preparedStatement.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try{
                    connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
    // FUNCTION THAT GENERATES RANDOM ID

    private int generateId(){
        int Min = 1, Max = 100;
        range = Max;
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }
    // FUNCTION THAT CHECKS IF USER ID ALREADY EXIST
    protected String validateId(){
        Connection connection = null;
        Set<Integer> idTab = new HashSet<>();
        PreparedStatement preparedStatement = null;
        int id = generateId();
        boolean findUniqueId = false;
        Alert validateAlert = new Alert(Alert.AlertType.NONE);
        // ADD IDS FROM DATABASE TO ARRAYLIST
        try {
            // ACCESS DATABASE
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
            // MYSQL SERVER
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
            preparedStatement = connection.prepareStatement("select user_id FROM users");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                idTab.add(resultSet.getInt("user_id"));
            }
            if (idTab.size() >= range) {
                throw new AlertException("Maintenance break, we need to increase id range");
            } else {
            // LOOKING FOR DUPLICATE
            if (idTab.contains(id)) {
                while (!findUniqueId) {
                    id = generateId();
                    if (!idTab.contains((id))) {
                        findUniqueId = true;
                    }
                }
                // ID IS UNIQUE
            } else {
                idTab.add(id);
            }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            // CLOSE ALL CONNECTIONS
        } catch (ClassNotFoundException exception){
            exception.printStackTrace();
        } catch (AlertException exception){
            validateAlert.setAlertType(Alert.AlertType.ERROR);
            validateAlert.setContentText(exception.getMessage());
            validateAlert.showAndWait();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
        return String.valueOf(id);
    }
    // FUNCTION TO MANIPULATE USER BALANCE
    protected void balanceModification(String balance, String user_id, String actualBalance, boolean depositId, boolean withdrawId, boolean transferId, Integer guestId){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement0 = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        double changingBalance = Double.parseDouble(balance);
        double userBalance = Double.parseDouble(actualBalance);
        double deposit = userBalance+changingBalance;
        double withdraw = userBalance-changingBalance;
        double transferBalance;
        Alert balanceAlert = new Alert(Alert.AlertType.NONE);

        try {
            // ACCESS DATABASE
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
            // MYSQL SERVER
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
            // DEPOSIT BUTTON
            if (depositId){
                if (changingBalance == 0){
                    throw new AlertException("No changes were made");
                } else {
                    preparedStatement = connection.prepareStatement("UPDATE users SET balance = ? WHERE user_id = ?");
                    preparedStatement.setDouble(1, deposit);
                    preparedStatement.setInt(2, Integer.parseInt(user_id));
                    preparedStatement.executeUpdate();
                }
                // WITHDRAW BUTTON
            } else if (withdrawId) {
                if (changingBalance == 0){
                    throw new AlertException("No changes were made");
                } else if (userBalance<changingBalance) {
                    throw new AlertException("You don't have enough money to proceed this operation");
                } else {
                    preparedStatement = connection.prepareStatement("UPDATE users SET balance = ? WHERE user_id = ?");
                    preparedStatement.setDouble(1, withdraw);
                    preparedStatement.setInt(2, Integer.parseInt(user_id));
                    preparedStatement.executeUpdate();
                    changeText = true;
                }
                // TRANSFER BUTTON
            } else if (transferId) {
                if (userBalance<changingBalance) {
                    throw new AlertException("You don't have enough money to proceed this operation");
                } else {
                    preparedStatement0 = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
                    preparedStatement0.setInt(1, guestId);
                    ResultSet resultSet1 = preparedStatement0.executeQuery();
                    if (resultSet1.next()){
                        if (Objects.equals(user_id, String.valueOf(guestId))){
                            throw new AlertException("You can't transfer money to yourself");
                        } else {
                            changeText = true;
                            preparedStatement = connection.prepareStatement("UPDATE users SET balance = ? WHERE user_id = ?");
                            preparedStatement.setDouble(1, withdraw);
                            preparedStatement.setInt(2, Integer.parseInt(user_id));
                            preparedStatement.executeUpdate();
                            preparedStatement1 = connection.prepareStatement("SELECT balance FROM users WHERE user_id = ?");
                            preparedStatement1.setInt(1, guestId);
                            ResultSet resultSet = preparedStatement1.executeQuery();
                            while (resultSet.next()) {
                                double guestBalance = resultSet.getDouble("balance");
                                transferBalance = guestBalance + changingBalance;
                                preparedStatement2 = connection.prepareStatement("UPDATE users SET balance = ? WHERE user_id = ?");
                                preparedStatement2.setDouble(1, transferBalance);
                                preparedStatement2.setInt(2, guestId);
                                preparedStatement2.executeUpdate();
                            }
                        }
                    } else {
                        throw new AlertException("Receiver not found");
                    }
                }
            }
        } catch (AlertException exception){
            balanceAlert.setAlertType(Alert.AlertType.WARNING);
            balanceAlert.setHeaderText("System information");
            balanceAlert.setContentText(exception.getMessage());
            balanceAlert.showAndWait();

        } catch (SQLException e) {
            balanceAlert.setAlertType(Alert.AlertType.ERROR);
            balanceAlert.setHeaderText("System information");
            balanceAlert.setContentText("Please fill all necessary fields");
            balanceAlert.showAndWait();
            // CLOSE ALL CONNECTIONS
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
            if (preparedStatement0 != null){
                try{
                    preparedStatement0.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement1 != null){
                try{
                    preparedStatement1.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement2 != null){
                try{
                    preparedStatement2.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try{
                    connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
    // FUNCTION TO CHANGE PASSWORD
    protected void changePassword(ActionEvent event, String user_id, String oldPassword, String newPassword1, String newPassword2){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Alert passwordAlert = new Alert(Alert.AlertType.NONE);

        try {
            // ACCESS DATABASE
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
            // MYSQL SERVER
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
            preparedStatement = connection.prepareStatement("SELECT password FROM users WHERE user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt(user_id));
            resultSet = preparedStatement.executeQuery();
            // COMPARE OLD AND NEW PASSWORDS
            while (resultSet.next()){
                String password = resultSet.getString("password");
                if (password.equals(oldPassword) && newPassword1.equals(newPassword2)){
                    if (password.equals(newPassword1)){
                        passwordAlert.setAlertType(Alert.AlertType.INFORMATION);
                        passwordAlert.setHeaderText("SYSTEM INFORMATION");
                        passwordAlert.setContentText("New password can't be the same as old");
                        passwordAlert.showAndWait();
                    } else {
                        passwordAlert.setAlertType(Alert.AlertType.CONFIRMATION);
                        passwordAlert.setHeaderText("SYSTEM INFORMATION");
                        passwordAlert.setContentText("Do you really want to change password?");
                        Optional<ButtonType> result = passwordAlert.showAndWait();
                        if (result.isEmpty() || result.get() == ButtonType.OK) {
                            PreparedStatement preparedStatement1 = connection.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?");
                            preparedStatement1.setString(1, newPassword1);
                            preparedStatement1.setInt(2, Integer.parseInt(user_id));
                            preparedStatement1.executeUpdate();
                            changeScene(event, "Login.fxml", null, null, null, null);
                        }
                    }
                } else {
                    passwordAlert.setAlertType(Alert.AlertType.ERROR);
                    passwordAlert.setHeaderText("SYSTEM INFORMATION");
                    passwordAlert.setContentText("Passwords doesn't match");
                    passwordAlert.showAndWait();
                }
            }
        } catch (SQLException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
            // CLOSE ALL CONNECTIONS
        } finally {
            if (resultSet != null){
                try {resultSet.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (preparedStatement != null){
                try {preparedStatement.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try {connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
    // FUNCTION TO DELETE ACCOUNT
    protected void deleteAccount(ActionEvent event, String user_id){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Alert confirmAlert = new Alert(Alert.AlertType.NONE);
        try{
            confirmAlert.setAlertType(Alert.AlertType.CONFIRMATION);
            confirmAlert.setHeaderText("Do you really want to delete your account?");
            confirmAlert.setContentText("All remaining funds from your account will become the property of the bank");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.OK) {
                // ACCESS DATABASE
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                connection= DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/com/atm/projectatm/atm_database.accdb");
                // MYSQL SERVER
                //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_database", "username", "password");
                preparedStatement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?");
                preparedStatement.setInt(1, Integer.parseInt(user_id));
                preparedStatement.executeUpdate();
                changeScene(event, "Login.fxml", null, null, null, null);
            }
        } catch (SQLException | ClassNotFoundException exception){
            exception.printStackTrace();
            // CLOSE ALL CONNECTIONS
        } finally {
            if (preparedStatement != null){
                try{preparedStatement.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
            if (connection != null){
                try{ connection.close();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            }
        }
    }
}
