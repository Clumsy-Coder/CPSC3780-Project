package utilities;

import com.sun.istack.internal.NotNull;

import java.math.BigInteger;
import java.util.Vector;

/**
 * Class used for keeping track of messages for each client connected.<br>
 * Used in Client.java
 */
public class Conversation
{
	/**
	 * Who the client is communicating with
	 */
	private User recipient;
	/**
	 * Messages exchanged for the client and the recipient.
	 */
	private Vector<Message> conversation;
	/**
	 * Sequence number for the user.
	 */
	private BigInteger sequenceNumber;
	/**
	 * Sequence number for the recipient.
	 */
	private BigInteger recipientSequenceNumber;
	
	/**
	 * Constructor used to initialize the attributes.
	 * @param recipient The recipient information
	 * @param sequenceNumber Initial sequence number for the client
	 * @param recipientSequenceNumber Initial sequence number for the recipient
	 */
	public Conversation(@NotNull User recipient,
	                    @NotNull BigInteger sequenceNumber,
	                    @NotNull BigInteger recipientSequenceNumber)
	{
		this.recipient = recipient;
		conversation = new Vector<Message>();
		this.sequenceNumber = sequenceNumber;
		this.recipientSequenceNumber = recipientSequenceNumber;
		
	}//END CONSTRUCTOR Conversation(User)
	
	/**
	 * Adding a message to the conversation.
	 * @param message
	 */
	public void addMessage(@NotNull Message message)
	{
		conversation.add(message);
		
	}//END METHOD addMessage(Message)
	
	/**
	 * Increment the <b>client's</b> sequence number.
	 */
	public void incrementSequenceNumber()
	{
		sequenceNumber = sequenceNumber.add(BigInteger.ONE);
		
	}//END METHOD incrementSequenceNumber()
	
	/**
	 * Increment <b>recipient</b> sequence number()
	 */
	public void incrementRecipientSequenceNumber()
	{
		recipientSequenceNumber = recipientSequenceNumber.add(BigInteger.ONE);
		
	}//END METHOD incrementRecipientSequenceNumber()
	
	/**
	 * Return the <b>client's</b> sequence number
	 * @return sequence number
	 */
	public @NotNull BigInteger getSequenceNumber()
	{
		return sequenceNumber;
		
	}//END METHOD getSequenceNumber()
	
	/**
	 * Return the <b>recipient's</b> sequence number
	 * @return sequence number of the <b>recipient</b>
	 */
	public @NotNull BigInteger getRecipientSequenceNumber()
	{
		return recipientSequenceNumber;
		
	}//END METHOD getRecipientSequenceNumber()
	
	/**
	 * Return the recipient.
	 * @return User object
	 */
	public @NotNull User getRecipient()
	{
		return recipient;
		
	}//END METHOD getRecipient()
	
	/**
	 * Return conversation
	 * @return Conversation
	 */
	public @NotNull Vector<Message> getConversation()
	{
		return conversation;
		
	}//END METHOD getConversation()
	
}//END CLASS Conversation
