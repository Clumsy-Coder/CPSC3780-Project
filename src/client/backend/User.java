package client.backend;

import java.io.Serializable;

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
     * In order to be able to send object through a network
     */
    private static final long serialVersionUID = -3529564406812045479L;

    //CONSTRUCTORS

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
}
