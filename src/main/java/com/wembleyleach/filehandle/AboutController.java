package com.wembleyleach.filehandle;

import javafx.fxml.*;
import javafx.scene.image.*;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    @FXML
    private ImageView logo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logo.setImage(new Image("/file-handle-logo.png"));
    }
}
