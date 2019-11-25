module com.wembleyleach.filehandle {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;

    // FXML uses reflection to work so open up the controllers package
    // so FXML can access its internals.
    opens com.wembleyleach.filehandle to javafx.fxml;
    exports com.wembleyleach.filehandle;
}
