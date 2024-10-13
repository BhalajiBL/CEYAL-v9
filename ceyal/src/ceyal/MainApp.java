package ceyal;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private TableView<EventLog> tableView;
    private ObservableList<EventLog> logData;
    private Pane petriNetPane;
    private ScrollPane scrollPane;
    private Slider zoomSlider;
    private double scaleFactor = 1.0;
    private List<Circle> places;
    private Label totalTimeLabel;
    private double simulationTime = 0;
    private LineChart<Number, Number> performanceChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Process Mining Pro - MVP");

        logData = FXCollections.observableArrayList();
        tableView = new TableView<>(logData);
        initializeTableColumns();

        TextField filterField = createStylizedTextField("Filter by Event, Resource, Cost, or Duration...");
        setupFilterField(filterField);

        Button loadButton = createStylizedButton("Load Event Log", "#4CAF50");
        loadButton.setOnAction(e -> loadEventLog(primaryStage));

        Button visualizeButton = createStylizedButton("Visualize Process", "#2196F3");
        visualizeButton.setOnAction(e -> visualizeProcess());

        Button simulateButton = createStylizedButton("Run Simulation", "#FF5722");
        simulateButton.setOnAction(e -> simulateProcess());

        Button analyzeButton = createStylizedButton("Analyze Log", "#9C27B0");
        analyzeButton.setOnAction(e -> analyzeEventLog());

        Button conformanceButton = createStylizedButton("Conformance Check", "#FFC107");
        conformanceButton.setOnAction(e -> conformanceCheck(new ArrayList<>()));

        Button performanceButton = createStylizedButton("Performance Analysis", "#03A9F4");
        performanceButton.setOnAction(e -> performanceAnalysis());

        Button predictiveButton = createStylizedButton("Predictive Analysis", "#E91E63");
        predictiveButton.setOnAction(e -> predictiveAnalysis());

        zoomSlider = createZoomSlider();
        scrollPane = new ScrollPane();
        petriNetPane = new Pane();
        setupScrollPane();

        VBox leftPanel = createLeftPanel(Arrays.asList(
            loadButton, visualizeButton, simulateButton, analyzeButton, 
            conformanceButton, performanceButton, predictiveButton
        ), zoomSlider, filterField);
        
        SplitPane splitPane = createSplitPane();
        totalTimeLabel = createStylizedLabel("Total Simulation Time: 0.0");

        performanceChart = createPerformanceChart();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createTab("Process Visualization", scrollPane),
            createTab("Performance Chart", performanceChart),
            createTab("Event Log", tableView)
        );

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPanel);
        mainLayout.setCenter(tabPane);
        mainLayout.setBottom(new HBox(10, totalTimeLabel));
        BorderPane.setMargin(totalTimeLabel, new Insets(10));

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void initializeTableColumns() {
        TableColumn<EventLog, String> eventColumn = new TableColumn<>("Event");
        eventColumn.setCellValueFactory(cellData -> cellData.getValue().eventProperty());

        TableColumn<EventLog, String> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());

        TableColumn<EventLog, String> resourceColumn = new TableColumn<>("Resource");
        resourceColumn.setCellValueFactory(cellData -> cellData.getValue().resourceProperty());

        TableColumn<EventLog, String> costColumn = new TableColumn<>("Cost");
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty());

        TableColumn<EventLog, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        tableView.getColumns().addAll(eventColumn, timestampColumn, resourceColumn, costColumn, durationColumn);
    }

    private void setupFilterField(TextField filterField) {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            tableView.setItems(logData.filtered(log ->
                    log.eventProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    log.resourceProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    log.costProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    String.valueOf(log.durationProperty().get()).contains(newValue)));
        });
    }

    private Slider createZoomSlider() {
        Slider slider = new Slider(0.5, 2.0, 1.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(5);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleFactor = newVal.doubleValue();
            petriNetPane.setScaleX(scaleFactor);
            petriNetPane.setScaleY(scaleFactor);
        });

        return slider;
    }

    private void setupScrollPane() {
        petriNetPane.setMinWidth(800);
        petriNetPane.setMinHeight(1000);
        scrollPane.setContent(petriNetPane);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
                scaleFactor *= delta;
                petriNetPane.setScaleX(scaleFactor);
                petriNetPane.setScaleY(scaleFactor);
                event.consume();
            }
        });
    }

    private VBox createLeftPanel(List<Button> buttons, Slider slider, TextField filterField) {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #ECEFF1;");
        leftPanel.getChildren().addAll(buttons);
        leftPanel.getChildren().addAll(slider, filterField);
        return leftPanel;
    }

    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tableView, scrollPane);
        splitPane.setDividerPositions(0.3);
        return splitPane;
    }

    private void loadEventLog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Event Log File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            readEventLog(file);
        }
    }

    private void readEventLog(File file) {
        logData.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip the header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    logData.add(new EventLog(parts[0], parts[1], parts[2], parts[3], parts[4]));
                } else {
                    throw new IOException("Invalid format");
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event log loaded successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error reading the file: " + e.getMessage());
        }
    }

    private void visualizeProcess() {
        petriNetPane.getChildren().clear();
        places = new ArrayList<>();
        drawProcessModelFromLog();
    }

    private void drawProcessModelFromLog() {
        double startX = 100;
        double startY = 50;
        double horizontalGap = 200;
        double verticalGap = 100;

        List<Rectangle> transitions = new ArrayList<>();

        for (int i = 0; i < logData.size(); i++) {
            EventLog event = logData.get(i);
            double x = startX + (i % 5) * horizontalGap;
            double y = startY + (i / 5) * verticalGap;

            Circle placeNode = createPlaceNode(x, y, event);
            places.add(placeNode);

            Label placeLabel = createStylizedLabel("Place " + (i + 1));
            placeLabel.setLayoutX(x - 40);
            placeLabel.setLayoutY(y + 40);
            petriNetPane.getChildren().add(placeLabel);

            if (i > 0) {
                Circle previousPlace = places.get(i - 1);
                Rectangle transition = createTransitionNode(previousPlace.getCenterX() + horizontalGap / 2, y);
                transitions.add(transition);

                drawArrow(previousPlace, transition);
                drawArrow(transition, placeNode);

                // Simulate token movement between places
                simulateTokenMovement(previousPlace, transition, placeNode);
            }
        }
    }

    private Circle createPlaceNode(double x, double y, EventLog event) {
        Circle place = new Circle(30);
        place.setFill(Color.LIGHTBLUE);
        place.setCenterX(x);
        place.setCenterY(y);
        place.setEffect(new DropShadow(10, Color.GRAY));

        place.setOnMouseClicked(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Event Details",
                    String.format("Event: %s\nResource: %s\nCost: %s\nDuration: %s",
                            event.getEvent(), event.getResource(), event.getCost(), event.getDuration()));
        });

        petriNetPane.getChildren().add(place);
        return place;
    }

    private Rectangle createTransitionNode(double x, double y) {
        Rectangle transition = new Rectangle(80, 30);
        transition.setFill(Color.ORANGE);
        transition.setX(x);
        transition.setY(y);

        transition.setOnMouseClicked(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Transition", "This is a transition.");
        });

        petriNetPane.getChildren().add(transition);
        return transition;
    }

    private void drawArrow(Circle from, Rectangle to) {
        Line line = new Line(from.getCenterX(), from.getCenterY(), to.getX(), to.getY() + 15);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);
    }

    private void drawArrow(Rectangle from, Circle to) {
        Line line = new Line(from.getX() + 80, from.getY() + 15, to.getCenterX(), to.getCenterY());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);
    }

    private void simulateTokenMovement(Circle from, Rectangle through, Circle to) {
        Circle token = new Circle(10, Color.RED);
        token.setCenterX(from.getCenterX());
        token.setCenterY(from.getCenterY());

        petriNetPane.getChildren().add(token);

        TranslateTransition moveThrough = new TranslateTransition(Duration.seconds(1), token);
        moveThrough.setFromX(from.getCenterX());
        moveThrough.setFromY(from.getCenterY());
        moveThrough.setToX(through.getX() + 40);
        moveThrough.setToY(through.getY() + 15);

        TranslateTransition moveTo = new TranslateTransition(Duration.seconds(1), token);
        moveTo.setFromX(through.getX() + 40);
        moveTo.setFromY(through.getY() + 15);
        moveTo.setToX(to.getCenterX());
        moveTo.setToY(to.getCenterY());

        moveThrough.setOnFinished(e -> moveTo.play());
        moveThrough.play();
    }

    private Circle createActivityNode(double x, double y) {
        Stop[] stops = new Stop[]{new Stop(0, Color.LIGHTBLUE), new Stop(1, Color.DARKBLUE)};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null, stops);
        Circle circle = new Circle(30);
        circle.setFill(gradient);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setEffect(new DropShadow(10, Color.GRAY));
        petriNetPane.getChildren().add(circle);
        return circle;
    }

    private void drawArrowWithLabel(Circle from, Circle to, double duration, int frequency) {
        Line line = new Line(from.getCenterX(), from.getCenterY(), to.getCenterX(), to.getCenterY());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);

        double midX = (from.getCenterX() + to.getCenterX()) / 2;
        double midY = (from.getCenterY() + to.getCenterY()) / 2;
        Label label = createStylizedLabel(String.format("Duration: %.2f\nFrequency: %d", duration, frequency));
        label.setLayoutX(midX);
        label.setLayoutY(midY);
        petriNetPane.getChildren().add(label);
    }

    private void simulateProcess() {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(2), new Circle(5, Color.RED));
        transition.setFromX(places.get(0).getCenterX());
        transition.setFromY(places.get(0).getCenterY());
        transition.setToX(places.get(places.size() - 1).getCenterX());
        transition.setToY(places.get(places.size() - 1).getCenterY());
        
        petriNetPane.getChildren().add(transition.getNode());
        transition.play();
        
        simulationTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.durationProperty().get()))
                .sum();

        totalTimeLabel.setText(String.format("Total Simulation Time: %.2f", simulationTime));
        showAlert(Alert.AlertType.INFORMATION, "Simulation", "Simulation completed successfully.");
    }

    private void analyzeEventLog() {
        int totalEvents = logData.size();
        double totalCost = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.costProperty().get()))
                .sum();

        double totalDuration = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.durationProperty().get()))
                .sum();

        String analysisResult = String.format(
            "Total Events: %d\nTotal Cost: $%.2f\nTotal Duration: %.2f units",
            totalEvents, totalCost, totalDuration
        );

        showAlert(Alert.AlertType.INFORMATION, "Event Log Analysis", analysisResult);
    }

    private void conformanceCheck(List<String> predefinedModel) {
        List<String> actualModel = logData.stream()
                .map(EventLog::getEvent)
                .collect(Collectors.toList());

        List<String> missingSteps = new ArrayList<>(predefinedModel);
        missingSteps.removeAll(actualModel);

        List<String> extraSteps = new ArrayList<>(actualModel);
        extraSteps.removeAll(predefinedModel);

        String conformanceResult = String.format(
            "Missing Steps: %s\nExtra Steps: %s",
            String.join(", ", missingSteps),
            String.join(", ", extraSteps)
        );

        showAlert(Alert.AlertType.INFORMATION, "Conformance Check Results", conformanceResult);
    }

    private void performanceAnalysis() {
        double averageTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getDuration()))
                .average().orElse(0);

        double averageCost = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getCost()))
                .average().orElse(0);

        String performanceResult = String.format(
            "Average Time: %.2f units\nAverage Cost: $%.2f",
            averageTime, averageCost
        );

        showAlert(Alert.AlertType.INFORMATION, "Performance Analysis", performanceResult);
        updatePerformanceChart();
    }

    private void predictiveAnalysis() {
        double averageTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getDuration()))
                .average().orElse(0);

        List<EventLog> predictedDelays = logData.stream()
                .filter(log -> Double.parseDouble(log.getDuration()) > averageTime)
                .collect(Collectors.toList());

        String delays = predictedDelays.stream()
                .map(log -> String.format("%s (Duration: %s)", log.getEvent(), log.getDuration()))
                .collect(Collectors.joining("\n"));

        String predictionResult = "Predicted Delays:\n" + delays;
        showAlert(Alert.AlertType.INFORMATION, "Predictive Analysis", predictionResult);
    }

    private LineChart<Number, Number> createPerformanceChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event Index");
        yAxis.setLabel("Duration");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Event Duration Performance");
        return lineChart;
    }

    private void updatePerformanceChart() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Event Duration");

        for (int i = 0; i < logData.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, Double.parseDouble(logData.get(i).getDuration())));
        }

        performanceChart.getData().clear();
        performanceChart.getData().add(series);
    }

    private Button createStylizedButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px;", color));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private TextField createStylizedTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-font-size: 14px;");
        return textField;
    }

    private Label createStylizedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

