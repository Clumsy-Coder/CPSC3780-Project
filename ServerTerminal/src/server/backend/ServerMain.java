package server.backend;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Umar on 2016-11-08.
 */
public class ServerMain
{
	public static void main(String [] args) throws
	                                        IOException,
	                                        ClassNotFoundException
	{
		String serverName;
		String connectingServerIP;
		int port;
		int connectServerPort;
		Server server = null;
		if(args.length == 2)
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
			
			server = new Server(serverName, port);
			server.startServer();
			
		}
		else if(args.length == 4)
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
			connectingServerIP = args[2];
			connectServerPort = Integer.parseInt(args[3]);
			
			server = new Server(serverName, port, connectingServerIP, connectServerPort);
//			server.startServer();
//			System.out.println("Stopping server");
//			server.stopServer();
		}
		else
		{
			System.out.println("Invalid number of arguments. Need 2");
			System.out.println("serverName port");
			return;
		}
		
		boolean keepGoing = true;
		Scanner scan      = new Scanner(System.in);
		
		while(keepGoing)
		{
			if(!keepGoing)
			{
				break;
			}
			
			String line = scan.nextLine();
			if(line.equalsIgnoreCase("CONNECT"))
			{
				System.out.print("IP address: ");
				String newIpaddress = scan.nextLine();
				System.out.println("Port number");
				int newServerPort = Integer.parseInt(scan.nextLine());
				
				server.connectServer(newIpaddress, newServerPort);
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
		}
		
		System.out.println("Stopping server");
		server.stopServer();
		System.out.println("Server stopped");
		
	}
	
}
