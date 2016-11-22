package utilities;

import com.sun.istack.internal.NotNull;

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
	
}//END CLASS UserNetworkInfo
