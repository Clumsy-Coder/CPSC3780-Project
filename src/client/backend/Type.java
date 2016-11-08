package client.backend;

/**
 * Enum used for keeping track what type of message is being sent.<br>
 * SEND: for sending a message<br>
 * GET: for retrieving a message<br>
 * ACK: for acknowledging a message has been recieved<br>
 * USER: for sending information about what user is connected to the server.
 * DISCONNECT: for notifying the server the client will be disconnecting
 */
public enum Type
{
    SEND, GET, ACK, USER, DISCONNECT
}
