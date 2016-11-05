package client.gui;/**
 * Created by Umar on 2016-11-03.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientChatMain extends Application
{

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws
	                                      Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("clientGUI.fxml"));
		primaryStage.setTitle("Client Chat");
		primaryStage.setScene(new Scene(root, 750, 500));
		primaryStage.setResizable(false);
		primaryStage.show();
	}
}
