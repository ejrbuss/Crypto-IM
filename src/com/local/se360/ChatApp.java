package com.local.se360;

import java.awt.Toolkit;

import javafx.application.Application;
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
	
	@Override
	public void start(Stage primary) {
	
		final AnchorPane root = new AnchorPane();
		final VBox configuration = new VBox();
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
		
		//root.getChildren().add(configuration);
		AnchorPane.setTopAnchor(configuration, 0.0);
		
		// --- Connection --- //
		// Connection status
		// Connection button (ONLY for client)
		if (connector instanceof Client) {		
			Button connectbtn = new Button("Connect");
			root.getChildren().add(connectbtn);
			AnchorPane.setTopAnchor(connectbtn, height/3);
		}
		
		// --- Message Log --- //
		// Previous messages
		ObservableList<String> messages = FXCollections.observableArrayList();
		ListView<String> messageLog = new ListView<String>(messages);
		messageLog.prefWidthProperty().bind(root.widthProperty());
		
		// --- Message Entry --- //
		// Message entry text box
		TextArea textarea = new TextArea();
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
				messages.add(textarea.getText());
				textarea.clear();
				messageLog.scrollTo(messageLog.getItems().size()-1);
			}
		});
		
		root.getChildren().add(textarea);
		AnchorPane.setBottomAnchor(textarea, 0.0);

		root.getChildren().add(messageLog);
		AnchorPane.setBottomAnchor(messageLog, textarea.getPrefHeight());
		
		// Run system.exit() on close
		primary.setOnCloseRequest(e -> System.exit(0));

		// --- Show --- //
		primary.setTitle(connector.name());
		primary.setScene(new Scene(root, width, height));
		primary.setX(connector.name().equals("Server") ? 0 : width);
		primary.setY(0);
		primary.show();
	}

}
