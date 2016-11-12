package utilities;

import java.math.BigInteger;
import java.util.Random;
import java.util.Vector;

public class Conversation
{
	private User recipient;
	private Vector<Message> conversation;
	private BigInteger sequenceNumber;
	private BigInteger recipientSequenceNumber;
	
	public Conversation(User recipient)
	{
		this.recipient = recipient;
		conversation = new Vector<Message>();
		
	}
	
	public void addMesage(Message message)
	{
		conversation.add(message);
		
	}
	
	public void incrementSequenceNumber()
	{
		sequenceNumber = sequenceNumber.add(BigInteger.ONE);
	}
	
	public BigInteger getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	public BigInteger getRecipientSequenceNumber()
	{
		return recipientSequenceNumber;
	}
	
	public void setRecipientSequenceNumber(BigInteger recipientSequenceNumber)
	{
		this.recipientSequenceNumber = recipientSequenceNumber;
	}
	
	public User getRecipient()
	{
		return recipient;
	}
	
	public Vector<Message> getConversation()
	{
		return conversation;
	}
	
	public void setSequenceNumber(BigInteger sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}
}
