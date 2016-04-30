package com.wembleyleach.filehandle;

import com.wembleyleach.filehandle.enums.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileHandleController implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private ComboBox<RenameOption> renameOptions;

    @FXML
    private ComboBox<NameLocation> nameLocationOptions;

    @FXML
    private ComboBox<NameLocation> formatNameLocationOptions;

    @FXML
    private ComboBox<NameFormat> nameFormatOptions;

    @FXML
    private Button cancel;

    @FXML
    private Button rename;

    @FXML
    private TextField findText;

    @FXML
    private TextField replaceText;

    @FXML
    private TextField addText;

    @FXML
    private TextField customFormat;

    @FXML
    private TextField startAt;

    @FXML
    private ListView<String> baseNames = new ListView<>();

    @FXML
    private ListView<String> newBaseNames = new ListView<>();

    private List<File> files;

    @FXML
    private Group addGroup;

    @FXML
    private Group replaceGroup;

    @FXML
    private Group formatGroup;

    private Stage primaryStage;
    private Alert alert;
    private RenameOption currentReplaceOption;
    private NameLocation currentAddTextNameLocationOption;
    private NameFormat currentNameFormatOption;
    private NameLocation currentNameFormatLocationOption;

    private final FileChooser fileChooser = new FileChooser();
    private final static String USER_DIRECTORY = System.getProperty("user.home");

    private LivePreviewBehavior[] livePreviewBehaviors;
    private Group[] optionGroups;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.setInitialDirectory(new File(USER_DIRECTORY));
        currentReplaceOption = RenameOption.REPLACE;

        livePreviewBehaviors = new LivePreviewBehavior[] { new ReplaceTextLivePreview(this) , new AddTextLivePreview(this) , new FormatTextLivePreview(this) };
        optionGroups = new Group[] { replaceGroup, addGroup, formatGroup };

        initAlert();
        initMenuBar();
        initRenameOptions();
        initRenameButton();
        initNameLocations();
        initFormatOptions();
        showGroup(optionGroups[0]);
    }

    private void initAlert() {
        alert = new Alert(Alert.AlertType.WARNING);
    }

    private void initMenuBar() {
        menuBar.setUseSystemMenuBar(true);
    }

    private void initRenameOptions() {
        renameOptions.getItems().addAll(RenameOption.values());
        renameOptions.setTooltip(new Tooltip("Select an option to rename your files"));
        renameOptions.setOnAction(this::renameOptionsAction);
    }

    private void renameOptionsAction(ActionEvent actionEvent) {
        RenameOption option = RenameOption.values()[renameOptions.getSelectionModel().getSelectedIndex()];
        showGroup(optionGroups[option.ordinal()]);
        currentReplaceOption = option;
        getOrInitializeStage().sizeToScene();
    }

    private void showGroup(Group group) {
        hideGroups();
        group.setVisible(true);
        group.setManaged(true);
    }

    private void hideGroups() {
        Arrays.stream(optionGroups)
                .forEach(FileHandleController::hideGroup);
    }

    private static void hideGroup(Group group) {
        group.setVisible(false);
        group.setManaged(false);
    }

    public void showAlertWithMessage(String message) {
        alert.setContentText(message);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> alert.close());
    }

    private void performFileRename(ActionEvent actionEvent) {
        if(files == null || files.size() == 0) return;
        List<File> movedFiles = new ArrayList<>(files.size());
        for(int i = 0; i < files.size(); i++) {
            try {
                Path oldFilePath = files.get(i).toPath();
                Path movedFile = Files.move(oldFilePath, oldFilePath.resolveSibling(newBaseNames.getItems().get(i)));
                movedFiles.add(movedFile.toFile());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        baseNames.getItems().setAll(newBaseNames.getItems());
        files = movedFiles;
    }

    private Stage getOrInitializeStage() {
        if(primaryStage == null)
            primaryStage = (Stage) renameOptions.getScene().getWindow();

        return primaryStage;
    }

    private void initRenameButton() {
        rename.setOnAction(this::performFileRename);
    }

    private void initNameLocations() {
        nameLocationOptions.getItems().addAll(NameLocation.values());
        nameLocationOptions.setTooltip(new Tooltip("Select where your text should be applied."));
        nameLocationOptions.setOnAction(event -> {
            currentAddTextNameLocationOption = NameLocation.values()[nameLocationOptions.getSelectionModel().getSelectedIndex()];
        });
    }

    private void initFormatOptions() {
        nameFormatOptions.getItems().addAll(NameFormat.values());
        nameFormatOptions.setTooltip(new Tooltip("Select a format to use when renaming your files."));
        nameFormatOptions.setOnAction(this::updateCurrentNameFormatOption);

        formatNameLocationOptions.getItems().addAll(NameLocation.values());
        formatNameLocationOptions.setTooltip(new Tooltip("Select where your text should be applied."));
        formatNameLocationOptions.setOnAction(this::updateCurrentNameFormatLocationOption);
    }

    private void updateCurrentNameFormatOption(ActionEvent actionEvent) {
        currentNameFormatOption = NameFormat.values()[nameFormatOptions.getSelectionModel().getSelectedIndex()];
    }

    private void updateCurrentNameFormatLocationOption(ActionEvent actionEvent) {
        currentNameFormatLocationOption = NameLocation.values()[formatNameLocationOptions.getSelectionModel().getSelectedIndex()];
    }

    public void livePreviewFileRename(KeyEvent event) {
        boolean shouldDisableRename = ((TextField) event.getSource()).getText().trim().isEmpty();
        rename.setDisable(shouldDisableRename);
        if(!shouldDisableRename)
            livePreviewBehaviors[currentReplaceOption.ordinal()].livePreview();
    }

    public void addFiles(ActionEvent actionEvent) {
        files = fileChooser.showOpenMultipleDialog(null);
        if(files != null) {
            List<String> rawBaseNames = files.stream()
                    .map(File::getName)
                    .collect(Collectors.toList());

            baseNames.getItems().setAll(rawBaseNames);
            newBaseNames.getItems().setAll(rawBaseNames);
        } else {
            showAlertWithMessage("There was an issue selecting your files. Please try again.");
        }
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public ListView<String> getBaseNames() {
        return baseNames;
    }

    public NameLocation getCurrentNameFormatLocationOption() {
        return currentNameFormatLocationOption;
    }

    public NameFormat getCurrentNameFormatOption() {
        return currentNameFormatOption;
    }

    public NameLocation getCurrentAddTextNameLocationOption() {
        return currentAddTextNameLocationOption;
    }

    public ListView<String> getNewBaseNames() {
        return newBaseNames;
    }

    public FileChooser getFileChooser() {
        return fileChooser;
    }

    public TextField getAddText() {
        return addText;
    }

    public TextField getFindText() {
        return findText;
    }

    public TextField getReplaceText() {
        return replaceText;
    }

    public TextField getCustomFormat() {
        return customFormat;
    }

    public TextField getStartAt() {
        return startAt;
    }
}

