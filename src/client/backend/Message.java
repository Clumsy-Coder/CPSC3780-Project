package client.backend;

import java.io.Serializable;

/**
 * Created by Umar on 2016-11-02.
 */
public class Message implements Serializable
{
    //ATTRIBUTES
    private Type    messageType;
    private User    user;
    private String  source;
    private String  destination;
    private Object  payload;        //NULL allowed. either send a String message, or user info

    //CONSTRUCTORS
    public Message(User user, Type messageType, String source, String destination, Object payload)
    {
        this.user = user;
        this.messageType = messageType;
        this.source = source;
        this.destination = destination;
        this.payload = payload;
    }

    //METHODS
    public Type getMessageType()
    {
        return messageType;
    }

    public User getUser()
    {
        return user;
    }

    public String getSource()
    {
        return source;
    }

    public String getDestination()
    {
        return destination;
    }

    public Object getPayload()
    {
        return payload;
    }
}
