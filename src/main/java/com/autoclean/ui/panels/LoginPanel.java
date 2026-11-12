package com.autoclean.ui.panels;

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla de inicio de sesión (Login) en JavaFX para AutoClean.
 * Valida las credenciales de demostración académica ('luis' y 'esteban' con clave '1234').
 */
public class LoginPanel extends StackPane {

    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final TextField txtPasswordVisible;
    private final Button btnTogglePassword;
    private final Label lblError;
    private final Consumer<String> onLoginSuccess;
    private boolean passwordShown = false;

    public LoginPanel(Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.txtUsername = new TextField();
        this.txtPassword = new PasswordField();
        this.txtPasswordVisible = new TextField();
        this.btnTogglePassword = new Button("Mostrar");
        this.lblError = new Label("Usuario o contraseña incorrectos");

        getStyleClass().add("login-background");
        setAlignment(Pos.CENTER);

        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(400);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);

        // Logotipo y marca AutoClean
        Label logoBadge = new Label("AC");
        logoBadge.getStyleClass().add("login-logo-badge");

        Label brandName = new Label("AUTOCLEAN • DETAILING & CAR CARE");
        brandName.getStyleClass().add("login-brand-name");

        // Título y subtítulo requeridos
        VBox titleGroup = new VBox(4);
        titleGroup.setAlignment(Pos.CENTER);
        Label lblTitle = new Label("Bienvenido a AutoClean");
        lblTitle.getStyleClass().add("login-title");
        Label lblSubtitle = new Label("Inicia sesión para continuar");
        lblSubtitle.getStyleClass().add("login-subtitle");
        titleGroup.getChildren().addAll(lblTitle, lblSubtitle);

        // Campo de usuario
        VBox userBox = new VBox(5);
        Label lblUser = new Label("Usuario");
        lblUser.getStyleClass().add("field-label");
        txtUsername.setPromptText("Ingresa tu usuario");
        txtUsername.getStyleClass().add("text-field");
        txtUsername.setOnAction(e -> {
            if (passwordShown) {
                txtPasswordVisible.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
        });
        userBox.getChildren().addAll(lblUser, txtUsername);

        // Campo de contraseña con opción de mostrar/ocultar y Enter para iniciar sesión
        VBox passBox = new VBox(5);
        Label lblPass = new Label("Contraseña");
        lblPass.getStyleClass().add("field-label");

        txtPassword.setPromptText("Ingresa tu contraseña");
        txtPassword.getStyleClass().add("text-field");
        txtPasswordVisible.setPromptText("Ingresa tu contraseña");
        txtPasswordVisible.getStyleClass().add("text-field");
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);

        // Sincronizar campos al presionar Enter o cambiar de vista
        txtPassword.setOnAction(e -> handleLogin());
        txtPasswordVisible.setOnAction(e -> handleLogin());

        StackPane passInputStack = new StackPane(txtPassword, txtPasswordVisible);
        HBox.setHgrow(passInputStack, Priority.ALWAYS);

        btnTogglePassword.getStyleClass().add("login-toggle-btn");
        btnTogglePassword.setOnAction(e -> togglePasswordVisibility());

        HBox passRow = new HBox(8, passInputStack, btnTogglePassword);
        passRow.setAlignment(Pos.CENTER_LEFT);
        passBox.getChildren().addAll(lblPass, passRow);

        // Mensaje de error (oculto por defecto)
        lblError.getStyleClass().add("login-error-label");
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setVisible(false);
        lblError.setManaged(false);

        // Botón Iniciar sesión
        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin());

        card.getChildren().addAll(logoBadge, brandName, titleGroup, userBox, passBox, lblError, btnLogin);
        getChildren().add(card);
    }

    private void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            txtPasswordVisible.requestFocus();
            btnTogglePassword.setText("Ocultar");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.requestFocus();
            btnTogglePassword.setText("Mostrar");
        }
    }

    /**
     * Valida las credenciales de prueba sin distinguir mayúsculas/minúsculas en el usuario
     * y validando exactamente la contraseña.
     *
     * @param username nombre de usuario ingresado
     * @param password contraseña ingresada
     * @return true si las credenciales corresponden a un administrador autorizado
     */
    public static boolean validateCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String cleanUser = username.trim();
        return (cleanUser.equalsIgnoreCase("luis") || cleanUser.equalsIgnoreCase("esteban"))
                && "1234".equals(password);
    }

    private void handleLogin() {
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = passwordShown ? txtPasswordVisible.getText() : txtPassword.getText();
        if (password == null) {
            password = "";
        }

        if (validateCredentials(username, password)) {
            lblError.setVisible(false);
            lblError.setManaged(false);
            String displayName = username.equalsIgnoreCase("luis") ? "Luis" : "Esteban";
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(displayName);
            }
        } else {
            lblError.setText("Usuario o contraseña incorrectos");
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    /**
     * Limpia el formulario de inicio de sesión al cerrar sesión.
     */
    public void resetForm() {
        txtUsername.clear();
        txtPassword.clear();
        lblError.setVisible(false);
        lblError.setManaged(false);
        if (passwordShown) {
            togglePasswordVisibility();
        }
        txtUsername.requestFocus();
    }
}

