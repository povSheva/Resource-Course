package com.example.demo.ui;

import com.example.demo.entity.FileEntity;
import com.example.demo.ui.controllers.ResourceController;
import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.util.stream.Stream;

public class ResourceManagerApp extends Application {

    private double xOffset, yOffset;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        // Без системного заголовка
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // Title-bar
        HBox titleBar = new HBox(8);
        titleBar.setPadding(new Insets(12, 20, 12, 16));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // Основное содержимое
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(0, 10, 10, 10));

        content.setLeft(buildLeftBar());
        content.setCenter(buildCenter());
        content.setRight(buildPreviewBox());

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
        ResourceController resourceController = new ResourceController();
        exportBtn.setOnAction(resourceController::onExportClick);

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
        Button addBtn = new Button("+ Добавить ресурс"); addBtn.setMaxWidth(Double.MAX_VALUE);
        ListView<String> filters = new ListView<>(FXCollections.observableArrayList("All","PDF","Images","Links"));
        filters.setMaxHeight(120);
        VBox box = new VBox(10, new Label("Pinned"), pinned, addBtn, new Label("Filter"), filters);
        box.setPadding(new Insets(10)); box.setPrefWidth(200);
        return box;
    }

    private VBox buildCenter() {
        TableView<FileEntity> table = createTable();
        Label status = new Label(table.getItems().size() + " записей");
        VBox box = new VBox(5, table, status); box.setPadding(new Insets(10,0,0,0));
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

    private TableView<FileEntity> createTable(){
        TableView<FileEntity> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<FileEntity,String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(c -> new TableCell<>(){
            @Override protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty||item==null){setGraphic(null);} else {
                    FileEntity r=getTableView().getItems().get(getIndex());
                    Label icon=new Label(switch(r.getType()){case"PDF"->"📄";case"Image"->"🖼️";default->"🔗";});
                    setGraphic(new HBox(5, icon, new Label(item)));
                }
            }
        });
        TableColumn<FileEntity,String> typeCol=new TableColumn<>("Тип"); typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<FileEntity,String> dateCol=new TableColumn<>("Дата"); dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        table.getColumns().addAll(nameCol,typeCol,dateCol);

        return table;
    }

    private VBox buildPreviewBox(){
        Label title=new Label("Course Outline"); title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label desc=new Label("An overview of the course content, including topics, objectives, and evaluation methods."); desc.setWrapText(true);
        VBox box=new VBox(10, title, desc, new Separator(), new HBox(10,new Button("⭳"),new Button("🔖")));
        box.setPadding(new Insets(10)); box.setPrefWidth(250); return box;
    }
}