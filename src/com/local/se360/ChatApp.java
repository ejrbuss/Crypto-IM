package com.local.se360;

import java.awt.Toolkit;

import com.local.se360.Connector.Message;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
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
	
	private ObservableList<String> messages = FXCollections.observableArrayList();
	private ListView<String> messageLog = new ListView<String>(messages);
	private TextArea textarea = new TextArea();
	private Button connectbtn = new Button("Connect");
	private final AnchorPane root = new AnchorPane();
	private final VBox configuration = new VBox();
	private boolean connected = false; 	// steal this from Client/Server?
	
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
		Text status = new Text("Connection Status: " + (connected ? "connected" : "not connected"));
		root.getChildren().add(status);
		AnchorPane.setTopAnchor(status, 0.0);
		
		// Connection button (ONLY for client)
		AnchorPane.setTopAnchor(connectbtn, height/3);
		connectbtn.setOnMouseClicked(e -> {
			authenticateView();
		});
		
		// --- Message Log --- //
		// Previous messages
		messageLog.prefWidthProperty().bind(root.widthProperty());
		
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
		
		connector.listen((Message m) -> {
			Platform.runLater(() -> {
				messages.add(m.message);
				messageLog.scrollTo(messageLog.getItems().size()-1);				
			});
		});
		
		// Run system.exit() on close
		primary.setOnCloseRequest(e -> System.exit(0));

		// --- Show --- //
		primary.setTitle(connector.name());
		primary.setScene(new Scene(root, width, height));
		primary.setX(connector.name().equals("Server") ? 0 : width);
		primary.setY(0);
		primary.show();
		
		//configureConnectionView();
		IMView();
	}
	
	public void configureConnectionView() {
		root.getChildren().clear();	// also deletes the connection status. don't want to do that.
		
		root.getChildren().add(configuration);
		
		if (connector instanceof Client) {		
			root.getChildren().add(connectbtn);
		}
	}
	
	public void authenticateView() {
		Text password = new Text("enter your password");
		root.getChildren().clear();
		root.getChildren().add(password);
		AnchorPane.setTopAnchor(password, 25.0);
		
	}
	
	public void IMView() {
		root.getChildren().clear();
		
		root.getChildren().add(textarea);
		AnchorPane.setBottomAnchor(textarea, 0.0);

		root.getChildren().add(messageLog);
		AnchorPane.setBottomAnchor(messageLog, textarea.getPrefHeight());
	}
	
}
