package utilities;

import com.sun.istack.internal.NotNull;

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
	/**
	 * Sequence number for keeping track of messages.
	 */
	private BigInteger sequenceNumber;
    /**
     * In order to be able to send object through a network
     */
    private static final long serialVersionUID = -3529564406812045479L;

    //CONSTRUCTORS
	
	/**
	 * Constructor that initializes attributes, and randomly generates sequence number.
	 * @param username The username of the user
	 */
	public User(@NotNull String username)
	{
		this.username = username;
		Random rand = new Random();
		sequenceNumber = new BigInteger(14, rand);
		System.out.println(this.username + " > sequence number: " + sequenceNumber);
		
	}//END CONSTRUCTOR User(String)

    /**
     * Default constructor that initializes attributes, and randomly generates sequence number.
     * @param username The unique username of the client
     * @param firstName First name of the client
     * @param lastName Last name of the client
     */
    public User(@NotNull String username, @NotNull String firstName, @NotNull String lastName)
    {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
	    Random rand = new Random();
	    sequenceNumber = new BigInteger(14, rand);
	    System.out.println(this.username + " > sequence number: " + sequenceNumber);
	    
    }//END CONSTRUCTOR User(String, String, String)

    //METHODS

    /**
     * Return the username
     * @return Username of the user
     */
    public final @NotNull String getUsername()
    {
        return username;
	    
    }//END METHOD getUsername

    /**
     * Return firstname of the client
     * @return Firstname of the user
     */
    public final @NotNull String getFirstName()
    {
        return firstName;
	    
    }//END METHOD getFirstName()

    /**
     * Return lastname of the client
     * @return Lastname of the user
     */
    public final @NotNull String getLastName()
    {
        return lastName;
	    
    }//END METHOD getLastName()
	
	/**
	 * Return the User's sequence number
	 * @return Sequence number of the user
	 */
	public @NotNull BigInteger getSequenceNumber()
	{
		return sequenceNumber;
		
	}//END METHOD getSequenceNumber()
	
	/**
	 * Sets sequence number for the user.
	 * @param sequenceNumber Sequence number
	 */
	public void setSequenceNumber(@NotNull BigInteger sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
		
	}//END METHOD setSequenceNumber(BigInteger)
	
}//END CLASS User
