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
		Server server = new Server("Server1");
		server.startServer();
		System.out.println("random text in ServerMain.java");
		server.stopServer();
	}
}
