package client.backend;

import java.io.Serializable;

/**
 * Class used for creating a message.
 * This object is used for sending
 * a message to another client or
 * sending information about the user.
 */
public class Message implements Serializable
{
    //ATTRIBUTES
    /**
     *
     */
    private Type    messageType;
    private User    user;
    private String  source;
    private String  destination;
    private Object  payload;        //NULL allowed. either send a String message, or user info

    //CONSTRUCTORS

    /**
     * Default constructor used for creating a message
     * @param user The sender's information
     * @param messageType The type of message, SEND, GET, ACK, USER
     * @param source The source IP address
     * @param destination The destination IP address
     * @param payload The message.
     */
    public Message(User user, Type messageType, String source, String destination, Object payload)
    {
        this.user = user;
        this.messageType = messageType;
        this.source = source;
        this.destination = destination;
        this.payload = payload;
    }

    //METHODS

    /**
     * Returns message Type
     * @return
     */
    public Type getMessageType()
    {
        return messageType;
    }

    /**
     * Returns the user information of the sender
     * @return
     */
    public User getUser()
    {
        return user;
    }

    /**
     * Returns the IP address of the sender
     * @return
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Returns the IP address of the destination
     * @return
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * Returns the message
     * @return
     */
    public Object getPayload()
    {
        return payload;
    }
}
