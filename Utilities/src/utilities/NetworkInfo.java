package utilities;

import java.net.InetAddress;

/**
 * Created by Umar on 2016-11-11.
 */
public class NetworkInfo
{
	private InetAddress ipAddress;
	private int         port;
	
	public NetworkInfo(InetAddress ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		
	}
	
	public InetAddress getIpAddress()
	{
		return ipAddress;
	}
	
	public int getPort()
	{
		return port;
	}
}
