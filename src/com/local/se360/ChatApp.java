package com.local.se360;

import java.awt.Toolkit;

import com.local.se360.Connector.Message;
import com.local.se360.Connector.Status;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class ChatApp extends Application {
	
	private static final double screenWidth = Toolkit
		.getDefaultToolkit()
		.getScreenSize()
		.getWidth();
	private static final double screenHeight = Toolkit
		.getDefaultToolkit()
		.getScreenSize()
		.getHeight();
	
	private static Connector connector;
	
	public static void connect(Connector connector) {
		ChatApp.connector = connector;
		launch();
	}
	
	private final ObservableList<String> messages = FXCollections.observableArrayList();
	private final ListView<String> messageLog = new ListView<String>(messages);
	private final TextArea textarea = new TextArea();
	private final Button connectbtn = new Button();
	private final Text status = new Text("Connection Status: " + connector.status().message);
	private final Text error = new Text();
	private final Text prompt = new Text("Enter your password: ");
	private final TextField password = new TextField();
	private final AnchorPane root = new AnchorPane();
	private final VBox configuration = new VBox();
	
	@Override
	public void start(Stage primary) {
	
		final double width   = screenWidth / 2.0;
		final double height  = screenHeight - 70;
		
		// TODO
		
		// --- Connection configuration --- //
		// Check box 1 confidentiality
		// Check box 2 Integrity
		// Check box 3 Authentication
		CheckBox cb1 = new CheckBox("Confidentiality");
		CheckBox cb2 = new CheckBox("Integrity");
		CheckBox cb3 = new CheckBox("Authentication");
		configuration.getChildren().addAll(cb1, cb2, cb3);
		AnchorPane.setTopAnchor(configuration, 25.0);

		// --- Connection --- //
		// Connection status
		AnchorPane.setTopAnchor(status, 0.0);
		AnchorPane.setTopAnchor(error, height/2);
		
		// Connection button / Start server button
		if (connector instanceof Client) {
			connectbtn.setText("Connect");
		} else if (connector instanceof Server) {
			connectbtn.setText("Start Server");
		}
		connectbtn.setOnMouseClicked(e -> {
			if (cb1.isSelected()) connector.requireConfidentiality(true);
			if (cb2.isSelected()) connector.requireIntegrity(true);
			if (cb3.isSelected()) connector.requireAuthentication(true);
			
			// FIXME - uncomment when implemented
			//Status connection = connector.connect();
			Status connection = new Status(true, "Connected");
			
			if (!connection.success) {
				error.setText(connection.message);
				root.getChildren().add(error);				
			}
			else {
				status.setText("Connection Status: " + connector.status().message);
				if (cb3.isSelected()) authenticateView();
				else IMView();
			}
		});
		AnchorPane.setTopAnchor(connectbtn, height/3);
		
		// --- Password Authentication --- //
		// Prompt
		AnchorPane.setTopAnchor(prompt, 25.0);
		
		// Password entry text box
		password.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER) {
				e.consume();
			}
		});
		password.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				
				// TODO
				// Authenticate the password.
				
				IMView();
			}
		});
		AnchorPane.setTopAnchor(password, 50.0);
		
		// --- Message Log --- //
		// Previous messages
		messageLog.prefWidthProperty().bind(root.widthProperty());
		connector.listen((Message m) -> {
			Platform.runLater(() -> {
				messages.add(m.message);
				messageLog.scrollTo(messageLog.getItems().size()-1);				
			});
		});
		
		// --- Message Entry --- //
		// Message entry text box
		textarea.prefWidthProperty().bind(root.widthProperty());
		textarea.setPrefHeight(height/4);
		textarea.setWrapText(true);
		textarea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER) {
				e.consume();
			}
		});
		textarea.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				Message message = new Message(connector.name(), textarea.getText());
				connector.send(message);
				messages.add(message.message);
				messageLog.scrollTo(messageLog.getItems().size()-1);
				textarea.clear();
			}
		});
		AnchorPane.setBottomAnchor(textarea, 0.0);
		AnchorPane.setBottomAnchor(messageLog, textarea.getPrefHeight());
		
		// Run system.exit() on close
		primary.setOnCloseRequest(e -> System.exit(0));

		// --- Show --- //
		primary.setTitle(connector.name());
		primary.setScene(new Scene(root, width, height));
		primary.setX(connector.name().equals("Server") ? 0 : width);
		primary.setY(0);
		primary.show();
		
		configureConnectionView();
	}
	
	public void configureConnectionView() {
		root.getChildren().clear();
		root.getChildren().add(status);
		root.getChildren().add(configuration);
		root.getChildren().add(connectbtn);
	}
	
	public void authenticateView() {
		root.getChildren().clear();
		root.getChildren().add(status);
		root.getChildren().addAll(prompt, password);
	}
	
	public void IMView() {
		root.getChildren().clear();
		root.getChildren().add(status);
		root.getChildren().add(textarea);
		root.getChildren().add(messageLog);
	}
	
}
