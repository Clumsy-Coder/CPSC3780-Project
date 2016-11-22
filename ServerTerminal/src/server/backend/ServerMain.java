package server.backend;

import java.io.IOException;

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
		int port;
		if(args.length == 2)
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
			
		}
		else
		{
			System.out.println("Invalid number of arguments. Need 2");
			System.out.println("serverName port");
			return;
		}
		
		Server server = new Server(serverName, port);
		server.startServer();
		System.out.println("Stopping server");
		server.stopServer();
	}
}
