package client.gui;

import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Umar
 *         Class used to respond to events from clientGUI.fxml
 */
public class ClientChatController implements Initializable
{
	private ArrayList<Label> users = new ArrayList<Label>();
	@FXML
	private JFXListView<Label> userList;
	@FXML
	private JFXListView<Label> conversationList;
	@FXML
	private StackPane stackPane;
	@FXML
	private JFXTextField username;
	@FXML
	private JFXTextField firstname;
	@FXML
	private JFXTextField lastname;
	@FXML
	private JFXTextField serverIP;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

		//open dialog that contains text field for username, firstname,
		//lastname and server IP address

		JFXDialogLayout content = new JFXDialogLayout();
		content.setHeading(new Text("Setup"));
		VBox vbox = new VBox(10);
		vbox.getChildren()
				.addAll(username, firstname, lastname, serverIP);
		content.setBody(vbox);
		JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
		JFXButton button = new JFXButton("Connect");
		button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Username: " + username.getText());
				System.out.println("Firstname: " + firstname.getText());
				System.out.println("Lastname: " + lastname.getText());
				System.out.println("ServerIP: " + serverIP.getText());
				dialog.close();
			}
		});

		content.setActions(button);
		dialog.show();

		for (int i = 0; i < 20; i++)
		{
			users.add(new Label("User " + i));
		}

//		userList.itemsProperty().bind(listProerty);
		userList.setItems(FXCollections.observableArrayList(users));
//		conversationList.setItems(FXCollections.observableArrayList(users));


	}

	@FXML
	public void clientSetup(ActionEvent event)
	{
		System.out.println("Username: " + username);
		System.out.println("Firstname: " + firstname);
		System.out.println("Lastname: " + lastname);
		System.out.println("ServerIP: " + serverIP);
	}
}
