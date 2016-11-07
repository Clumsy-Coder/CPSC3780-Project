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

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

		//open dialog that contains text field for username, firstname,
		//lastname and server IP address

		JFXDialogLayout content = new JFXDialogLayout();
		content.setHeading(new Text("Setup"));
		VBox vbox = new VBox(10);
		vbox.getChildren()
				.addAll(new JFXTextField("Username"), new JFXTextField("Firstname"), new JFXTextField("Lastname"),
				        new JFXTextField("Server IP"));
		content.setBody(vbox);
		JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
		JFXButton button = new JFXButton("Connect");
		button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				dialog.close();
			}
		});

		content.setActions(button);
		dialog.show();

//			JFXDialogLayout dialogBoxLayout = FXMLLoader.load(getClass().getResource("resource/dialog.fxml"));
//			JFXDialog       dialogBox       = new JFXDialog(new StackPane(), dialogBoxLayout,/*
//			                                                JFXDialog.DialogTransition.CENTER);*/
//			JFXDialog dialogBox = new JFXDialog();
//
//			dialogBox.setContent(dialogBoxLayout);
//			dialogBox.setTransitionType(JFXDialog.DialogTransition.CENTER);
//			dialogBox.show();

		for (int i = 0; i < 20; i++)
		{
			users.add(new Label("User " + i));
		}

//		userList.itemsProperty().bind(listProerty);
		userList.setItems(FXCollections.observableArrayList(users));
//		conversationList.setItems(FXCollections.observableArrayList(users));


	}
}
