module com.atm.projectatm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires ucanaccess;


    opens com.atm.projectatm to javafx.fxml;
    exports com.atm.projectatm;
}