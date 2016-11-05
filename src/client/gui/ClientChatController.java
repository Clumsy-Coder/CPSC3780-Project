package client.gui;

import com.jfoenix.controls.JFXListView;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;

/**
 * Created by Umar on 2016-11-03.
 */
public class ClientChatController implements Initializable
{
	private ArrayList<Label>   users = new ArrayList<Label>();
	@FXML
	private JFXListView<Label> userList;
	@FXML
	private JFXListView<Label> conversationList;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		for(int i = 0; i < 20; i++)
		{
			users.add(new Label("User " + i));
		}

//		userList.itemsProperty().bind(listProerty);
		userList.setItems(FXCollections.observableArrayList(users));
//		conversationList.setItems(FXCollections.observableArrayList(users));



	}
}
