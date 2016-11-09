package client.backend;

import utilities.User;

import java.util.Scanner;

/**
 * Created by Umar on 2016-11-08.
 */
public class ClientMain
{
	public static void main(String[] args)
	{
		int    port     = 5555;
		String serverIP = "localhost";
		String username = "Anonymous";
		//String username = "Anonymous2";

		User   user   = new User(username);
		Client client = new Client(serverIP, port, user);

		if (!client.connect())
		{
			return;
		}

		Scanner scan      = new Scanner(System.in);
		boolean keepGoing = true;
		while (keepGoing)
		{
			System.out.print(username + "> ");
			String line = scan.nextLine();
			//if logout was typed
			if (line.equalsIgnoreCase("LOGOUT"))
			{
				//send a message to the server that you are disconnecting.
				keepGoing = false;
				System.out.println("Logged off");

			}
			else
			{
				client.sendMessage(line, new User("Anonymous2"));
				//client.sendMessage(line, new User("Anonymous"));
			}
		}
		client.disconnect();

	}
}
