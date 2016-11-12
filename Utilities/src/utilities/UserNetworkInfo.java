package utilities;

import java.net.InetAddress;

/**
 * Created by Umar on 2016-11-11.
 */
public class UserNetworkInfo
{
	private InetAddress ipAddress;
	private int         port;
	private User        user;
	
	public UserNetworkInfo(InetAddress ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		
	}
	
	public UserNetworkInfo(InetAddress ipAddress, int port, User user)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.user = user;
	}
	
	public InetAddress getIpAddress()
	{
		return ipAddress;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public User getUser()
	{
		return user;
	}
}
