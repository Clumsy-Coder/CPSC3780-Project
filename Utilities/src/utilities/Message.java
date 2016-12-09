package utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

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
	 * Contains the sequence number of the message. <b>CANNOT</b> be null when two clients are sending a message to each other.<br>
	 * Can be null when Client connects to the server, or when server is sending clients info about connected clients.
	 */
	private BigInteger  sequenceNumber;
	/**
	 * The type of message being sent or received
	 */
	private MessageType messageType;
	/**
	 * Who is sending the message
	 */
	private User        source;
	/**
	 * Who is receiving the message
	 */
	private User        destination;
	/**
	 * Contains text message being sent by the client or when the Server is sending info about the connected clients.
	 */
	private Object      payload;        //NULL allowed. either send a String message, or user info
	/**
	 * In order to be able to send object through a network
	 */
	private static final long serialVersionUID = 355757530704006971L;

	//CONSTRUCTORS

	/**
	 * Default constructor used for creating a message
	 *
	 * @param messageType The type of message
	 * @param source      The source IP address
	 * @param destination The destination IP address
	 * @param payload     The message.
	 */
	public Message(@NotNull MessageType messageType,
	               @NotNull User source,
	               @Nullable User destination,
	               @NotNull Object payload)
	{
		this.messageType    = messageType;
		this.source         = source;
		this.destination    = destination;
		this.payload        = payload;
		
	}//END CONSTRUCTOR Message(MessageType, User, User, Object)

	//METHODS

	/**
	 * Returns message Type
	 *
	 * @return The message type
	 */
	public @NotNull MessageType getMessageType()
	{
		return messageType;
		
	}//END METHOD getMessageType()

	/**
	 * Returns the User who is sending the Message
	 *
	 * @return User object
	 */
	public @NotNull User getSource()
	{
		return source;
		
	}//END METHOD getSource()

	/**
	 * Returns the User who is receiving the Message
	 *
	 * @return The User the message is being sent to
	 */
	public @Nullable User getDestination()
	{
		return destination;
		
	}//END METHOD getDestination()

	/**
	 * Returns the message payload
	 *
	 * @return Message payload.
	 */
	public Object getPayload()
	{
		return payload;
		
	}//END METHOD getPayload()

	/**
	 * Returns the sequence number
	 * @return Sequence number of the message
	 */
	public @Nullable BigInteger getSequenceNumber()
	{
		return sequenceNumber;
		
	}//END METHOD getSequenceNumber()
	
	/**
	 * Set the sequence number for the message
	 * @param sequenceNumber Sequence number
	 */
	public void setSequenceNumber(@NotNull BigInteger sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
		
	}//END METHOD setSequenceNumber(BigInteger)
	
	/**
	 * Sets the MessageType
	 * @param messageType Message type
	 */
	public void setMessageType(@NotNull MessageType messageType)
	{
		this.messageType = messageType;
		
	}//END METHOD setMessageType(MessageType)
	
	/**
	 * Set the payload
	 * @param payload The object being set
	 */
	public void setPayload(@NotNull Object payload)
	{
		this.payload = payload;
		
	}//END METHOD setPayload(Object)
	
}//END CLASS Message
