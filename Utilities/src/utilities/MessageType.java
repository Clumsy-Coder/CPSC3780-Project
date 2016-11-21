package utilities;

/**
 * Enum used for keeping track what type of message is being sent.<br>
 */
public enum MessageType
{
	/**
	 * If a client is sending a message
	 */
    SEND,
	/**
	 * When client is sending a GET request or when server is responding to the GET request<br>
	 * Used for retrieving messages for the client.
	 */
    GET,
	/**
	 * When sending an acknowledgment that the message has been received by the client.
	 */
	ACK,
	/**
	 * When the server is sending information about newly connected clients on the server
	 */
	USERS,
	/**
	 * When the client is disconnecting the server or when server is sending info to other clients who has disconnected.
	 */
	DISCONNECT,
	/**
	 * When a client is connecting to the server
	 */
	CONNECT,
	/**
	 * When a new new server is connected to the existing server.
	 */
	NEW_SERVER,
	/**
	 * When a server is disconnecting
	 */
	SERVER_DISCONNECT
}
