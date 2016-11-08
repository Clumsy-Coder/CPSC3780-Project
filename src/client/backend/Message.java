package client.backend;

import java.io.Serializable;
import java.math.BigInteger;

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
	private BigInteger sequenceNumber;
	private Type       messageType;
	private User       user;
	private User       source;
	private User       destination;
	private Object     payload;        //NULL allowed. either send a String message, or user info
	/**
	 * In order to be able to send object through a network
	 */
	private static final long serialVersionUID = 355757530704006971L;

	//CONSTRUCTORS

	/**
	 * Default constructor used for creating a message
	 *
	 * @param user        The sender's information
	 * @param messageType The type of message, SEND, GET, ACK, USER
	 * @param source      The source IP address
	 * @param destination The destination IP address
	 * @param payload     The message.
	 */
	public Message(User user, Type messageType, User source, User destination, Object payload)
	{
		this.user           = user;
		this.messageType    = messageType;
		this.source         = source;
		this.destination    = destination;
		this.payload        = payload;
	}

	//METHODS

	/**
	 * Returns message Type
	 *
	 * @return
	 */
	public Type getMessageType()
	{
		return messageType;
	}

	/**
	 * Returns the user information of the sender
	 *
	 * @return
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Returns the IP address of the sender
	 *
	 * @return
	 */
	public User getSource()
	{
		return source;
	}

	/**
	 * Returns the IP address of the destination
	 *
	 * @return
	 */
	public User getDestination()
	{
		return destination;
	}

	/**
	 * Returns the message
	 *
	 * @return
	 */
	public Object getPayload()
	{
		return payload;
	}
}