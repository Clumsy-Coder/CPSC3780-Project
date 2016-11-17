package utilities;

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
	private BigInteger  sequenceNumber;
	private MessageType messageType;
	private User        source;
	private User        destination;
	private Object      payload;        //NULL allowed. either send a String message, or user info
	/**
	 * In order to be able to send object through a network
	 */
	private static final long serialVersionUID = 355757530704006971L;

	//CONSTRUCTORS

	/**
	 * Default constructor used for creating a message
	 *
	 * @param messageType The type of message, SEND, GET, ACK, USER
	 * @param source      The source IP address
	 * @param destination The destination IP address
	 * @param payload     The message.
	 */
	public Message(MessageType messageType, User source, User destination, Object payload)
	{
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
	public MessageType getMessageType()
	{
		return messageType;
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

	/**
	 * Returns the sequence number
	 * @return
	 */
	public BigInteger getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	public void setSequenceNumber(BigInteger sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
//		System.out.println("Message: sequence number : " + this.sequenceNumber );
	}
	
	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}
}
