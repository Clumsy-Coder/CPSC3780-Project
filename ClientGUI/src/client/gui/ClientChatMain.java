package client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Class used for initialzing the Client Chat GUI
 */
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
		Parent root = FXMLLoader.load(getClass().getResource("resource/clientGUI.fxml"));
		primaryStage.setTitle("Client Chat");
		primaryStage.setScene(new Scene(root, 750, 500));
		primaryStage.setResizable(false);
		primaryStage.show();
	}
}
