package latina;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import latina.integracion.emfc.EMFContainer;
import latina.vista.controlador.Controlador;
import latina.vista.Eventos;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class VistaPrincipal extends Application {
    private WebView webView;
    private double xOffset = 0;
    private double yOffset = 0;
    private final Color TEXT_COLOR = Color.web("#FFFFFF"); // Azul claro
    private final String COLOR_HOVER_BOTONES = "#606060"; // Rojo oscuro para hover
    private final String TITLE_BAR_COLOR = "#333"; // Color más elegante para la barra
    private final String BORDER_COLOR = "#333"; // Color de borde a juego

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("LaTina - Gestión del Restaurante");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/latina/images/logo LaTina.png")));
        EMFContainer.getInstance().getEMF().createEntityManager();
        HBox titleBar = createTitleBar(primaryStage);
        webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        //URL htmlFile = VistaPrincipal.class.getResource("/latina/html/ventanaPrincipal.html");
        webEngine.load("https://amauryav-ucm.github.io/LaTina/src/main/resources/latina/html/iniciarSesion.html");
        configureJavaScriptBridge(webEngine);

        // Hacer que el WebView sea responsivo
        VBox.setVgrow(webView, Priority.ALWAYS);

        // Establecer el ancho preferido para el WebView
        webView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        webView.setPrefHeight(Region.USE_COMPUTED_SIZE);

        VBox root = new VBox(titleBar, webView);
        root.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 2px;");

        // Asegurar que el VBox root use todo el espacio disponible
        root.setPrefWidth(Region.USE_COMPUTED_SIZE);
        root.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(root, 1250, 700);

        // Agregar listener para manejar los cambios de tamaño
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            root.setPrefWidth(newVal.doubleValue());
            webView.setPrefWidth(newVal.doubleValue());
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            root.setPrefHeight(newVal.doubleValue());
            // Restar altura de la barra de título
            double titleBarHeight = titleBar.getHeight();
            webView.setPrefHeight(newVal.doubleValue() - titleBarHeight);
        });

        // Agregar listener para cuando la ventana cambia de estado de maximizado
        primaryStage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Cuando se maximiza
                root.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0px;");
            } else {
                // Cuando vuelve a su tamaño normal
                root.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 2px;");
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTitleBar(Stage primaryStage) {
        HBox titleBar = new HBox();
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setSpacing(5);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: " + TITLE_BAR_COLOR + ";");
        HBox.setHgrow(titleBar, Priority.ALWAYS);

        Text titleText = new Text("LaTina");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleText.setFill(TEXT_COLOR);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Crear un HBox para los botones de control
        HBox controlBox = new HBox(2);
        controlBox.setAlignment(Pos.CENTER_RIGHT);

        try {
            ImageView minimizeIcon = new ImageView(new Image(getClass().getResourceAsStream("/latina/images/minimizeBlanco.png")));
            ImageView maximizeIcon = new ImageView(new Image(getClass().getResourceAsStream("/latina/images/cuadradoBlanco.png")));
            ImageView closeIcon = new ImageView(new Image(getClass().getResourceAsStream("/latina/images/Xblanca.png")));

            configureIcon(minimizeIcon);
            configureIcon(maximizeIcon);
            configureIcon(closeIcon);

            Button minimizeButton = createWindowControlButton(minimizeIcon, event -> primaryStage.setIconified(true));
            Button maximizeButton = createWindowControlButton(maximizeIcon, event -> {
                primaryStage.setMaximized(!primaryStage.isMaximized());
                // Actualizar el estado de la interfaz
                resizeUI(primaryStage);
            });
            Button closeButton = createWindowControlButton(closeIcon, event -> System.exit(0));

            controlBox.getChildren().addAll(minimizeButton, maximizeButton, closeButton);
        } catch (Exception ex) {
            System.err.println("Error cargando iconos: " + ex.getMessage());
            // Botones de respaldo sin iconos
            Button minimizeButton = new Button("_");
            Button maximizeButton = new Button("□");
            Button closeButton = new Button("X");

            minimizeButton.setOnAction(event -> primaryStage.setIconified(true));
            maximizeButton.setOnAction(event -> {
                primaryStage.setMaximized(!primaryStage.isMaximized());
                resizeUI(primaryStage);
            });
            closeButton.setOnAction(event -> System.exit(0));

            addDarkRedHoverEffect(minimizeButton);
            addDarkRedHoverEffect(maximizeButton);
            addDarkRedHoverEffect(closeButton);

            controlBox.getChildren().addAll(minimizeButton, maximizeButton, closeButton);
        }

        setupWindowDragListeners(titleBar, primaryStage);

        titleBar.getChildren().addAll(titleText, spacer, controlBox);
        return titleBar;
    }

    private void resizeUI(Stage stage) {
        // Notificar a JavaScript que la ventana ha cambiado de tamaño
        if (webView != null && webView.getEngine() != null) {
            try {
                webView.getEngine().executeScript(
                "if (typeof window.onResize === 'function') { window.onResize(" +
                        stage.getWidth() + ", " + stage.getHeight() + ", " +
                        stage.isMaximized() + "); }"
                );
            } catch (Exception ex) {
                // Si falla el script, no afecta la funcionalidad principal
            }
        }
    }

    private Button createWindowControlButton(Node graphic, EventHandler<ActionEvent> event) {
        Button button = new Button();
        button.setGraphic(graphic);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
        button.setOnAction(event);
        addDarkRedHoverEffect(button);
        return button;
    }

    private void configureIcon(ImageView icon) {
        icon.setFitWidth(14);
        icon.setFitHeight(14);
    }

    private void addDarkRedHoverEffect(Button button) {
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: " + COLOR_HOVER_BOTONES + "; -fx-padding: 5px;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: transparent; -fx-padding: 5px;"));
    }

    private void setupWindowDragListeners(HBox titleBar, Stage primaryStage) {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            if (!primaryStage.isMaximized()) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                primaryStage.setMaximized(!primaryStage.isMaximized());
                resizeUI(primaryStage);
            }
        });
    }

    private void configureJavaScriptBridge(WebEngine webEngine) {
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", this);

                // Inicializar tamaño para JavaScript
                Stage stage = (Stage) webView.getScene().getWindow();
                resizeUI(stage);
            }
        });
    }

    public void changeScene(String nuevaEscena)
    {
        webView.getEngine().load("https://amauryav-ucm.github.io/LaTina/src/main/resources/latina/html/" + nuevaEscena);
    }

    public void accion(String eventoStr, Object datos) {
        Eventos evento = Eventos.valueOf(eventoStr);
        Controlador.getInstance(this).accion(evento, datos);
    }


    public WebView getWebView() {
        return webView;
    }
}