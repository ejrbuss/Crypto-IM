package com.local.se360;

import java.awt.Toolkit;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
	
		final StackPane root = new StackPane();
		final double width   = screenWidth / 2.0;
		final double height  = screenHeight;
		
		// TODO
		
		// --- Connection configuration --- //
		// Check box 1 confidentiality
		// Check box 2 Integrity
		// Check box 3 Authentication
		
		// --- Connection --- //
		// Connection status
		// Connection button (ONLY for client)
		
		// --- Message Log --- //
		// Previous messages
		
		// --- Message Entry --- //
		// Message entry text box
		
		// --- Show --- //
		primary.setTitle(connector.name());
		primary.setScene(new Scene(root, width, height));
		primary.setX(connector.name().equals("Server") ? 0 : width);
		primary.setY(0);
		primary.show();
	}

}
