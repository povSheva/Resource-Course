package com.example.demo.ui;

import com.example.demo.controllers.ResourceController;
import com.example.demo.controllers.SearchController;
import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileEntityService;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * JavaFX‑приложение для управления файлами.
 * <p>
 * Вся работа с базой и файловой системой делегирована {@link FileEntityService},
 * чтобы UI не знал ничего о {@link java.sql.Connection} и {@link java.nio.file.Path}.
 */
public class ResourceManagerApp extends Application {

    /* ===================================================================== */
    /* === Dependencies & State =========================================== */

    private final FileEntityService service = new FileEntityService(
            new FileEntityDao(),
            System.getProperty("repo.root", "exports"));
    private final ObservableList<FileEntity> fileItems = FXCollections.observableArrayList();

    private SearchController searchController;

    private TextField searchField;

    private double xOffset; // для перетаскивания окна
    private double yOffset;

    /* ===================================================================== */
    /* === Application entry‑point ======================================== */

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        HBox titleBar = buildTitleBar(primaryStage);

        // Загружаем данные --------------------------------------------------
        try {
            List<FileEntity> all = service.findAll();
            fileItems.setAll(all);
            searchController = new SearchController(fileItems);
        } catch (Exception ex) {
            showErrorAndExit("Не удалось загрузить список файлов", ex);
            return;
        }

        /* === UI layout =================================================== */
        BorderPane content = new BorderPane();
        content.setLeft(buildLeftBar());
        content.setCenter(buildCenter());
        content.setRight(buildPreviewBox());

        VBox rootPane = wrapWithRoundedWindow(titleBar, content);

        /* === Scene ======================================================= */
        Scene scene = new Scene(rootPane, 900, 600);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /* ===================================================================== */
    /* === Title bar (traffic lights, search, export) ====================== */

    private HBox buildTitleBar(Stage stage) {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(12, 20, 12, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // Traffic‑lights buttons ------------------------------------------
        StackPane btnClose = makeTrafficButton(Color.web("#ff5f57"), "×", e -> stage.close());
        StackPane btnMin   = makeTrafficButton(Color.web("#ffbd2e"), "–", e -> animateMinimize(stage));
        StackPane btnMax   = makeTrafficButton(Color.web("#28c840"), "+", e -> stage.setMaximized(!stage.isMaximized()));
        var lights = Stream.of(btnClose, btnMin, btnMax).toList();
        lights.forEach(btn -> {
            btn.setOnMouseEntered(ev -> lights.forEach(ResourceManagerApp::showGlyph));
            btn.setOnMouseExited (ev -> lights.forEach(ResourceManagerApp::hideGlyph));
        });

        // Search box -------------------------------------------------------
        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(420);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-border-color: #d0d4d9; -fx-background-color: #f9fafb; " +
                "-fx-padding: 6 12 6 36; " +
                "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        searchField.textProperty().addListener((obs, old, nw) ->
                searchController.onSearch(nw)
        );
        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 8));
        StackPane searchBox = new StackPane(searchField, searchIcon);

        // Export button ----------------------------------------------------
        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-font-size: 15px; -fx-font-family: 'Inter Semibold'; -fx-font-weight: 600; " +
                "-fx-text-fill: #0f1113; -fx-padding: 6 32; -fx-border-color: transparent;");
        exportBtn.setOnAction(new ResourceController(service, fileItems)::onExportClick);

        Region leftSpacer  = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        bar.getChildren().setAll(btnClose, btnMin, btnMax, leftSpacer, searchBox, rightSpacer, exportBtn);

        // Dragging the window ---------------------------------------------
        bar.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });

        stage.iconifiedProperty().addListener((obs, wasMin, isMin) -> {
            if (!isMin) restoreAnimation((Region) stage.getScene().getRoot());
        });

        return bar;
    }

    /* ===================================================================== */
    /* === Left bar (pinned + filter) ===================================== */

    private VBox buildLeftBar() {
        ListView<String> pinned = new ListView<>(FXCollections.observableArrayList("course-outline.pdf", "article-link"));
        pinned.setMaxHeight(100);

        Button addBtn = new Button("+ Добавить ресурс");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> onAddResource());

        ListView<String> filters = new ListView<>(FXCollections.observableArrayList("All", "PDF", "Images", "Links"));
        filters.setMaxHeight(120);

        VBox box = new VBox(10, new Label("Pinned"), pinned, addBtn, new Label("Filter"), filters);
        box.setPadding(new Insets(10));
        box.setPrefWidth(200);
        return box;
    }

    /* ===================================================================== */
    /* === Center: ListView with files ===================================== */

    private VBox buildCenter() {
        ListView<FileEntity> listView = createFileList();
        Label status = new Label();
        status.textProperty().bind(Bindings.size(fileItems).asString("%d записей"));

        VBox box = new VBox(5, listView, status);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private ListView<FileEntity> createFileList() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        ListView<FileEntity> lv = new ListView<>(fileItems);

        lv.setCellFactory(view -> new ListCell<>() {
            private final HBox row  = new HBox(10);
            private final ImageView icon = new ImageView();
            private final TextFlow nameFlow = new TextFlow();
            private final Label type = new Label();
            private final Label date = new Label();

            {
                icon.setFitWidth(24);
                icon.setFitHeight(24);
                HBox.setHgrow(nameFlow, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                row.setStyle(
                        "-fx-background-color: #ffffff; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-color: #e0e0e0;"
                );
                row.getChildren().addAll(icon, nameFlow, type, date);
            }

            @Override
            protected void updateItem(FileEntity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                // — иконка
                URL resUrl = getClass().getResource(switch (item.getType().toUpperCase()) {
                    case "PDF"   -> "/images/pdf.png";
                    case "IMAGE" -> "/images/image.png";
                    default      -> "/images/file.png";
                });
                icon.setImage(resUrl != null ? new Image(resUrl.toExternalForm()) : null);

                // — подсветка совпадения
                nameFlow.getChildren().clear();
                String fullName = item.getOrigName();
                String q = searchField.getText().toLowerCase();
                if (q != null && !q.isBlank()) {
                    String lower = fullName.toLowerCase();
                    int idx = lower.indexOf(q);
                    if (idx >= 0) {
                        // до
                        Text before = new Text(fullName.substring(0, idx));
                        // совпавшая часть — Label с жёлтым фоном
                        Label match = new Label(fullName.substring(idx, idx + q.length()));
                        match.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                        // после
                        Text after = new Text(fullName.substring(idx + q.length()));
                        nameFlow.getChildren().addAll(before, match, after);
                    } else {
                        nameFlow.getChildren().add(new Text(fullName));
                    }
                } else {
                    nameFlow.getChildren().add(new Text(fullName));
                }

                // — остальные поля
                type.setText(item.getType());
                date.setText(item.getAddedAt().format(fmt));

                setGraphic(row);
            }
        });

        lv.setPrefHeight(400);
        return lv;
    }


    /* ===================================================================== */
    /* === Right preview placeholder ====================================== */

    private VBox buildPreviewBox() {
        Label title = new Label("Course Outline");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label desc = new Label("An overview of the course content, including topics, objectives, and evaluation methods.");
        desc.setWrapText(true);
        HBox actions = new HBox(10, new Button("⭳"), new Button("🔖"));

        VBox box = new VBox(10, title, desc, new Separator(), actions);
        box.setPadding(new Insets(10));
        box.setPrefWidth(250);
        return box;
    }

    /* ===================================================================== */
    /* === Add resource flow ============================================== */

    private void onAddResource() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        try {
            FileEntity saved = service.uploadFile(file); // сохраняет файл + метадату
            // 4) Добавляем через SearchController, чтобы оба списка остались синхронизированы
            searchController.onAddNew(saved);
        } catch (Exception ex) {
            showError("Не удалось добавить файл", ex);
        }
    }

    /* ===================================================================== */
    /* === Helpers ========================================================= */

    private static StackPane makeTrafficButton(Color color, String glyph, EventHandler<MouseEvent> action) {
        Circle bg = new Circle(9, color);
        Label lbl = new Label(glyph);
        lbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        lbl.setMouseTransparent(true); lbl.setVisible(false);

        StackPane sp = new StackPane(bg, lbl);
        sp.setPrefSize(20, 20);
        sp.setCursor(Cursor.HAND);
        sp.setOnMouseClicked(action);
        sp.setUserData(lbl);
        return sp;
    }

    private static void showGlyph(StackPane b) { ((Label) b.getUserData()).setVisible(true); }
    private static void hideGlyph(StackPane b) { ((Label) b.getUserData()).setVisible(false); }

    private void animateMinimize(Stage st) {
        Region root = (Region) st.getScene().getRoot();
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.scaleXProperty(), 1),
                        new KeyValue(root.scaleYProperty(), 1),
                        new KeyValue(root.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.scaleXProperty(), .4, Interpolator.EASE_BOTH),
                        new KeyValue(root.scaleYProperty(), .4, Interpolator.EASE_BOTH),
                        new KeyValue(root.opacityProperty(), 0, Interpolator.EASE_BOTH))
        );
        tl.setOnFinished(e -> st.setIconified(true));
        tl.play();
    }

    private void restoreAnimation(Region root) {
        root.setScaleX(.4); root.setScaleY(.4); root.setOpacity(0);
        Timeline back = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.scaleXProperty(), .4),
                        new KeyValue(root.scaleYProperty(), .4),
                        new KeyValue(root.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(root.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(root.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );
        back.play();
    }

    private VBox wrapWithRoundedWindow(HBox titleBar, BorderPane content) {
        VBox rootPane = new VBox(titleBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        CornerRadii radii = new CornerRadii(15);
        rootPane.setBackground(new Background(new BackgroundFill(Color.WHITE, radii, Insets.EMPTY)));
        rootPane.setBorder(new Border(new BorderStroke(Color.web("#cccccc"), BorderStrokeStyle.SOLID, radii, new BorderWidths(1))));

        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        rootPane.setClip(clip);
        rootPane.layoutBoundsProperty().addListener((obs, o, n) -> {
            clip.setWidth(n.getWidth()); clip.setHeight(n.getHeight());
        });
        return rootPane;
    }

    private void showError(String msg, Throwable ex) {
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        new Alert(Alert.AlertType.ERROR, msg + "\n" + cause, ButtonType.OK).showAndWait();
    }

    private void showErrorAndExit(String msg, Throwable ex) {
        showError(msg, ex);
        System.exit(1);
    }
}