package org.arhub.robots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Kyle
 */
public class RobotKeyboardController extends Application {

	public static SerialPort port;

	@Override
	public void start(Stage primaryStage) {
		Label label1 = new Label("Com Port:");
		final TextField textField = new TextField();
		Set<String> heldKeys = new HashSet<>();

		Button btn = new Button();
		btn.setText("Connect");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				port = new SerialPort(textField.getText());
				try {
					port.openPort();
					port.setParams(SerialPort.BAUDRATE_9600,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (Exception e) {
				}
			}
		});

		HBox hb = new HBox();
		hb.getChildren().addAll(label1, textField, btn);
		hb.setSpacing(10);
		textField.setFocusTraversable(true);
		btn.setFocusTraversable(true);

		VBox root = new VBox();
		root.getChildren().addAll(hb, new KeyPressedView(heldKeys).getBox());
		Scene scene = new Scene(root, 300, 250);
		scene.getStylesheets().add("org/arhub/robots/style.css");

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private class KeyPressedView {
		private TitledPane pane = new TitledPane();
		private final HashMap<String, Label> keyToLabel = new HashMap<String, Label>();

		private KeyPressedView(final Set<String> heldKeys) {
			final HBox box = new HBox();
			box.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override
				public void handle(KeyEvent t) {
					String key = t.getText().toLowerCase();
					if (heldKeys.contains(key))
						return;
					heldKeys.add(key);
					Label label = new Label(key);
					label.getStyleClass().add("key");
					box.getChildren().add(label);
					keyToLabel.put(key, label);
					
					if(port==null) return;
					
					try {
						port.writeBytes((key + "D").getBytes());
					} catch (SerialPortException ex) {
						Logger.getLogger(
								RobotKeyboardController.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}

			});
			box.setOnKeyReleased(new EventHandler<KeyEvent>() {

				@Override
				public void handle(KeyEvent t) {
					String key = t.getText().toLowerCase();
					heldKeys.remove(key);
					Label label = keyToLabel.remove(key);
					box.getChildren().remove(label);
					
					if(port==null) return;
					
					try {
						port.writeBytes((key + "U").getBytes());
					} catch (SerialPortException ex) {
						Logger.getLogger(
								RobotKeyboardController.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}
			});
			box.setFocusTraversable(true);
			box.getStyleClass().add("box");

			pane = new TitledPane();
			pane.setText("Currently Held Keys");
			pane.setContent(box);
			pane.setFocusTraversable(false);
			pane.setCollapsible(false);
			pane.setExpanded(true);
			pane.setMinHeight(300);
		}

		public TitledPane getBox() {
			return pane;
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
