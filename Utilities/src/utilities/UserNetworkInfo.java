package utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Class used for keeping track who is connected to the server.<br>
 * Used by Server.java.<br><br>
 *
 * This class is used for sending/receiving info about the clients connected
 * when a new server connects to another server, or updating which user has
 * connected/disconnected to the server.<br>
 * The server uses this class to send/receive info about the server. When a
 * server is connecting to another server, it needs to keep track of IP and port number
 * when a message is to be sent from one server to another.<br><br>
 *
 * This is also used for clients.<br>
 * When a client connects to a server (direct connection, NOT from another server),
 * This class is created to keep track of the IP address, port and the User object
 * for the client who is connecting to the server. When the server is sending
 * a message to the client, it looks at the IP address associated with the name.
 */
public class UserNetworkInfo implements Serializable
{
	private static final long serialVersionUID = -3850770004132892410L;
	/**
	 * IP address for the user
	 */
	private InetAddress ipAddress;
	/**
	 * Port number for the user
	 */
	private int         port;
	/**
	 * User information
	 */
	private User        user;
	/**
	 * Keeps track of which server to send to when a message is being sent. Only used for clients.
	 */
	private User        server;
	
	/**
	 * Constructor initializing the object.
	 * @param ipAddress IP address
	 * @param port port number
	 * @param user User information
	 */
	public UserNetworkInfo(@NotNull InetAddress ipAddress, @NotNull int port, @NotNull User user)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.user = user;
		
	}//END CONSTRUCTOR UserNetworkInfo(InetAddress, int, User)
	
	/**
	 * Returns the IP address
	 * @return IP address
	 */
	public @NotNull InetAddress getIpAddress()
	{
		return ipAddress;
		
	}//END METHOD getIpAddress()
	
	/**
	 * Returns the port number
	 * @return port number
	 */
	public @NotNull int getPort()
	{
		return port;
		
	}//END METHOD getPort()
	
	/**
	 * Returns the User object
	 * @return User object
	 */
	public @NotNull User getUser()
	{
		return user;
		
	}//END METHOD getUser()
	
	/**
	 * Returns the server associated with the client. The server attribute is only set for clients
	 * @return The server associated with the client.
	 */
	public @Nullable User getServer()
	{
		return server;
		
	}//END METHOD getServer()
	
	/**
	 * Sets the server associated with the client. Only used for clients
	 * @param server Information about the server
	 */
	public void setServer(@NotNull User server)
	{
		this.server = server;
		
	}//END METHOD setServer(User)
	
	/**
	 * Sets the User object
	 * @param user Information about the User
	 */
	public void setUser(@NotNull User user)
	{
		this.user = user;
		
	}//END METHOD setUser(User)
	
}//END CLASS UserNetworkInfo
