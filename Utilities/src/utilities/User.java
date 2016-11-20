package utilities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

/**
 * Class used for identifying the user.<br>
 * This is created only once for the client.
 *      In terms of when the client opens the
 *      chat window.<br>
 * When the client connects to the server,
 * this object is sent to the server to keep
 * track of users connected.
 */
public final class User implements Serializable
{
    //ATTRIBUTES
    /**
     * Username when using the chat. Must be unique
     */
    private String  username;
    /**
     * First name of the user
     */
    private String  firstName;
    /**
     * Last name of the user
     */
    private String  lastName;
    
    private BigInteger sequenceNumber;

    /**
     * In order to be able to send object through a network
     */
    private static final long serialVersionUID = -3529564406812045479L;

    //CONSTRUCTORS

	public User(String username)
	{
		this.username = username;
		Random rand = new Random();
		sequenceNumber = new BigInteger(14, rand);
		System.out.println(this.username + " > sequence numeber: " + sequenceNumber);
	}

    /**
     * Default constructor
     * @param username The unique username of the client
     * @param firstName Firstname of the client
     * @param lastName Last name of the client
     */
    public User(String username, String firstName, String lastName)
    {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
	    Random rand = new Random();
	    sequenceNumber = new BigInteger(14, rand);
	    System.out.println(this.username + " > sequence numeber: " + sequenceNumber);
    }

    //METHODS

    /**
     * Return the username
     * @return
     */
    public final String getUsername()
    {
        return username;
    }

    /**
     * Return firstname of the client
     * @return
     */
    public final String getFirstName()
    {
        return firstName;
    }

    /**
     * Return lastname of the client
     * @return
     */
    public final String getLastName()
    {
        return lastName;
    }
	
	public BigInteger getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	public void setSequenceNumber(BigInteger sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}
}