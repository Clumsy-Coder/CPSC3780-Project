package server.backend;

import java.util.Scanner;

/**
 * Class used for running the Server class
 * Takes either 2 or 4 arguments
 * 2 arguments: starting the server.
 * 4 arguments: start the server and connect to another server.
 *
 */
public class ServerMain
{
	public static void main(String [] args)
	{
		String serverName;
		String connectingServerIP;
		int port;
		int connectServerPort;
		Server server = null;
		
		//if the arguments are
		//  server name
		//  port number
		if(args.length == 2)
		{
			try
			{
				serverName = args[0];
				port = Integer.parseInt(args[1]);
				
				server = new Server(serverName, port);
				server.startServer();
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid input for Server port. Must be a number. Try again");
				System.exit(-1);
			}
			
		}//END if(args.length == 2)
		
		//if the arguments are
		//  server name
		//  port number
		//  connecting server IP address
		//  connecting server port number.
		else if(args.length == 4)
		{
			try
			{
				serverName = args[0];
				port = Integer.parseInt(args[1]);
				connectingServerIP = args[2];
				connectServerPort = Integer.parseInt(args[3]);
				server = new Server(serverName, port, connectingServerIP, connectServerPort);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid input for Server port number or connecting server port number. Must be a number. Try again");
				System.exit(-1);
			}
			
		}//END else if(args.length == 4)
		
		else
		{
			System.out.println("Invalid number of arguments. Need 2 or 4 arguments");
			System.out.println("<Server name> <port>");
			System.out.println("<Server Name> <port> <connecting server IP address> <connecting server port>");
			return;
			
		}//END else
		
		
		boolean keepGoing = true;
		Scanner scan      = new Scanner(System.in);
		
		while(keepGoing)
		{
			if(!keepGoing)
			{
				break;
			}
			
			String line = scan.nextLine();
			if(line.equalsIgnoreCase("HELP"))
			{
				System.out.println("Commands available");
				System.out.println("\thelp - display help menu");
				System.out.println("\tconnect - connect to another server");
				System.out.println("\tservers - display the servers connected");
				System.out.println("\tclients - display clients connected to the server");
				System.out.println("\twhoami - display info about this server");
				System.out.println("\tshutdown - shutdown the server and exit");
			}
			
			if(line.equalsIgnoreCase("CONNECT"))
			{
				try
				{
					System.out.print("IP address: ");
					String newIpaddress = scan.nextLine();
					System.out.println("Port number");
					int newServerPort = Integer.parseInt(scan.nextLine());
					
					server.connectServer(newIpaddress, newServerPort);
				}
				catch (NumberFormatException e)
				{
					System.out.println("Invalid input for port number. Try again");
				}
			}
			
			else if(line.equalsIgnoreCase("SHUTDOWN"))
			{
				keepGoing = false;
			}
			
			else if (line.equalsIgnoreCase("SERVERS"))
			{
				server.printServers();
			}
			
			else if(line.equalsIgnoreCase("CLIENTS"))
			{
				server.printClients();
			}
			
			else if (line.equalsIgnoreCase("WHOAMI"))
			{
				server.whoami();
			}
			
			else
			{
				System.out.println("Invalid command.");
			}
			
		}//END while(keepGoing)
		
		System.out.println("Stopping server");
		server.stopServer();
		System.out.println("Server stopped");
		
	}//END METHOD main(String [] args)
	
}//END CLASS ServerMain
