package client.backend;

import java.io.Serializable;

/**
 * Class used for identifying the user.
 * This is created only once for the client.
 * When the client connects to the server,
 * this object is sent to the server to keep
 * track of users connected.
 */
public final class User implements Serializable
{
    //ATTRIBUTES
    private String  username;
    private String  firstName;
    private String  lastName;

    //CONSTRUCTORS
    public User(String username, String firstName, String lastName)
    {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    //METHODS
    public final String getUsername()
    {
        return username;
    }

    public final String getFirstName()
    {
        return firstName;
    }

    public final String getLastName()
    {
        return lastName;
    }
}
