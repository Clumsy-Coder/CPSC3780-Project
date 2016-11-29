package utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.net.InetAddress;

/**
 * Class used for keeping track who is connected to the server.<br>
 * Used by Server.java
 */
public class UserNetworkInfo
{
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
	
	public UserNetworkInfo(@NotNull InetAddress ipAddress,
	                       @NotNull int port,
	                       @NotNull User user,
	                       @NotNull User server)
	{
		this(ipAddress, port, user);
		this.server = server;
		
	}//END CONSTRUCTOR UserNetworkInfo(InetAddres, int, User, User)
	
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
	
//	public void setUser(@NotNull User user)
//	{
//		this.user = user;
//	}//END METHOD setUser(User)
	
	public @Nullable User getServer()
	{
		return server;
		
	}//END METHOD getServer()
	
	public void setServer(@NotNull User server)
	{
		this.server = server;
		
	}//END METHOD setServer(User)
	
}//END CLASS UserNetworkInfo
