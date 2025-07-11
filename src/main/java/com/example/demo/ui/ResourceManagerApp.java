package com.example.demo.ui;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;
import com.example.demo.controllers.ResourceController;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class ResourceManagerApp extends Application {

    FileEntityDao dao = new FileEntityDao();
    FileEntityService service = new FileEntityService(dao);
    ObservableList<FileEntity> fileItems = FXCollections
            .observableArrayList( dao.findAll() );

    private double xOffset, yOffset;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        // Без системного заголовка
        primaryStage.initStyle(StageStyle.TRANSPARENT);


        // Ошибки с подключением к БД или файлами
        try {
            List<FileEntity> all = dao.findAll();
            fileItems.setAll(all);
        } catch (Exception e) {

            e.printStackTrace();

            String cause = e.getCause() != null
                    ? e.getCause().getMessage()
                    : e.getMessage();
            Alert err = new Alert(Alert.AlertType.ERROR,
                    "Не удалось загрузить файлы:\n" + cause,
                    ButtonType.OK);
            err.showAndWait();
            return;
        }

        // Основное содержимое
        BorderPane content = new BorderPane();
        content.setLeft(buildLeftBar());
        content.setCenter(buildCenter());
        content.setRight(buildPreviewBox());

        // Title-bar
        HBox titleBar = new HBox(8);
        titleBar.setPadding(new Insets(12, 20, 12, 16));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // Корень с округлёнными краями
        VBox rootPane = new VBox(titleBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        CornerRadii radii = new CornerRadii(15);
        rootPane.setBackground(new Background(new BackgroundFill(Color.WHITE, radii, Insets.EMPTY)));
        rootPane.setBorder(new Border(new BorderStroke(Color.web("#cccccc"), BorderStrokeStyle.SOLID, radii, new BorderWidths(1))));
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        rootPane.setClip(clip);
        rootPane.layoutBoundsProperty().addListener((obs, o, n) -> { clip.setWidth(n.getWidth()); clip.setHeight(n.getHeight()); });

        // Traffic lights
        StackPane btnClose = makeIconButton(Color.web("#ff5f57"), "×", e -> primaryStage.close());
        StackPane btnMin   = makeIconButton(Color.web("#ffbd2e"), "–", e -> animateMinimize(primaryStage, rootPane));
        StackPane btnMax   = makeIconButton(Color.web("#28c840"), "+", e -> primaryStage.setMaximized(!primaryStage.isMaximized()));
        var traffic = Stream.of(btnClose, btnMin, btnMax).toList();
        traffic.forEach(btn -> {
            btn.setOnMouseEntered(ev -> traffic.forEach(ResourceManagerApp::showGlyph));
            btn.setOnMouseExited (ev -> traffic.forEach(ResourceManagerApp::hideGlyph));
        });

        // Поиск + Export
        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(420);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-border-color: #d0d4d9; -fx-background-color: #f9fafb; " +
                "-fx-padding: 6 12 6 36; " +          // слева больше отступ под иконку
                "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);
        StackPane.setMargin(searchIcon, new Insets(0,0,0,8));
        StackPane searchBox = new StackPane(searchField, searchIcon);

        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-font-size: 15px; -fx-font-family: 'Inter Semibold'; -fx-font-weight: 600; " +
                "-fx-text-fill: #0f1113; -fx-padding: 6 32; -fx-border-color: transparent;");

        // Поднимает тут экземпляр контроллера и событие на кнопку
        ResourceController controller = new ResourceController(service, fileItems);
        exportBtn.setOnAction(controller::onExportClick);


        Region leftSpacer  = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer,Priority.ALWAYS);
        titleBar.getChildren().setAll(btnClose, btnMin, btnMax, leftSpacer, searchBox, rightSpacer, exportBtn);

        // Drag window
        titleBar.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
        titleBar.setOnMouseDragged(e -> {
            if (!primaryStage.isMaximized()) {
                primaryStage.setX(e.getScreenX() - xOffset);
                primaryStage.setY(e.getScreenY() - yOffset);
            }
        });

        primaryStage.iconifiedProperty().addListener((obs, wasMin, isMin) -> {
            if (!isMin) restoreAnimation(rootPane); });

        Scene scene = new Scene(rootPane, 900, 600);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // === Сборка блоков ======================================================

    private VBox buildLeftBar() {
        ListView<String> pinned = new ListView<>(FXCollections.observableArrayList("course-outline.pdf", "article-link"));
        pinned.setMaxHeight(100);

        Button addBtn = new Button("+ Добавить ресурс");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> onAddResource());

        ListView<String> filters = new ListView<>(FXCollections.observableArrayList("All","PDF","Images","Links"));
        filters.setMaxHeight(120);
        VBox box = new VBox(10, new Label("Pinned"), pinned, addBtn, new Label("Filter"), filters);
        box.setPadding(new Insets(10)); box.setPrefWidth(200);
        return box;
    }

    // === Новый центр: ListView вместо TableView ===========================

    private VBox buildCenter() {
        ListView<FileEntity> listView = createFileList();
        Label status = new Label();
        status.textProperty().bind(
                Bindings.size(fileItems)
                        .asString("%d записей")
        );
        VBox box = new VBox(5, listView, status);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    // === Вспомогательные элементы ==========================================

    private StackPane makeIconButton(Color bg, String glyph, EventHandler<MouseEvent> action) {
        Circle circle = new Circle(9, bg); // диаметр ~18
        Label lbl = new Label(glyph);
        lbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.BLACK); lbl.setMouseTransparent(true); lbl.setVisible(false);
        StackPane sp = new StackPane(circle, lbl);
        sp.setPrefSize(20,20); sp.setCursor(Cursor.HAND); sp.setOnMouseClicked(action);
        sp.setUserData(lbl); return sp;
    }
    private static void showGlyph(StackPane b){ ((Label)b.getUserData()).setVisible(true);}
    private static void hideGlyph(StackPane b){ ((Label)b.getUserData()).setVisible(false);}

    private void restoreAnimation(Region root){
        root.setScaleX(.4); root.setScaleY(.4); root.setOpacity(0);
        Timeline back = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(root.scaleXProperty(),.4), new KeyValue(root.scaleYProperty(),.4), new KeyValue(root.opacityProperty(),0)),
                new KeyFrame(Duration.millis(250), new KeyValue(root.scaleXProperty(),1,Interpolator.EASE_BOTH), new KeyValue(root.scaleYProperty(),1,Interpolator.EASE_BOTH), new KeyValue(root.opacityProperty(),1,Interpolator.EASE_BOTH))
        ); back.play();
    }

    private void animateMinimize(Stage st, Region root){
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(root.scaleXProperty(),1), new KeyValue(root.scaleYProperty(),1), new KeyValue(root.opacityProperty(),1)),
                new KeyFrame(Duration.millis(250), new KeyValue(root.scaleXProperty(),.4,Interpolator.EASE_BOTH), new KeyValue(root.scaleYProperty(),.4,Interpolator.EASE_BOTH), new KeyValue(root.opacityProperty(),0,Interpolator.EASE_BOTH))
        ); tl.setOnFinished(e->st.setIconified(true)); tl.play();
    }

    // === Таблица ============================================================

    private ListView<FileEntity> createFileList() {

        ListView<FileEntity> listView = new ListView<>(fileItems);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        listView.setCellFactory(lv -> new ListCell<>() {
            private final HBox      row  = new HBox(10);
            private final ImageView icon = new ImageView();
            private final Label     name = new Label();
            private final Label     type = new Label();
            private final Label     date = new Label();

            {
                icon.setFitWidth(24);
                icon.setFitHeight(24);

                HBox.setHgrow(name, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                row.setStyle(
                        "-fx-background-color: #ffffff; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-color: #e0e0e0;"
                );
                row.getChildren().addAll(icon, name, type, date);
            }

                    @Override
                    protected void updateItem(FileEntity item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setGraphic(null);
                            return;
                        }

                        String iconPath;
                        switch (item.getType().toUpperCase()) {
                            case "PDF"   -> iconPath = "/images/pdf.png";
                            case "IMAGE" -> iconPath = "/images/image.png";
                            default      -> iconPath = "/images/file.png";
                        }

                        URL resUrl = getClass().getResource(iconPath);
                        if (resUrl == null) {
                            System.err.println("Icon not found: " + iconPath);
                            icon.setImage(null);
                        } else {
                            icon.setImage(new Image(resUrl.toExternalForm()));
                        }

                        name.setText(item.getOrigName());
                        type.setText(item.getType());
                        date.setText(item.getAddedAt().format(fmt));
                        setGraphic(row);
                    }
        });


        listView.setPrefHeight(400);
        return listView;
    }

    // Обработчик добавления ресурса: сохраняем в БД и в список
    private void onAddResource() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        FileEntity newFile = new FileEntity();
        newFile.setOrigName(file.getName());
        newFile.setType(detectTypeByExtension(file));
        newFile.setSizeBytes(file.length());
        newFile = dao.save(newFile);

        fileItems.add(newFile);
        fileItems.setAll(dao.findAll());
    }

    // Вспомогательный метод для определения типа по расширению
    private String detectTypeByExtension(File file) {
        String ext = "";
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) ext = name.substring(i+1).toLowerCase();
        switch (ext) {
            case "pdf":  return "PDF";
            case "png":
            case "jpg":
            case "jpeg": return "Image";
            case "doc":
            case "docx":return "Docx";
            default:     return ext.toUpperCase();
        }
    }

    private VBox buildPreviewBox(){
        Label title=new Label("Course Outline"); title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label desc=new Label("An overview of the course content, including topics, objectives, and evaluation methods."); desc.setWrapText(true);
        VBox box=new VBox(10, title, desc, new Separator(), new HBox(10,new Button("⭳"),new Button("🔖")));
        box.setPadding(new Insets(10)); box.setPrefWidth(250); return box;
    }
}