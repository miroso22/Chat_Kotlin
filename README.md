# Console chat
Simple chat written on kotlin.
Consists of 2 parts: Server and Client.
### Server
* Creates server socket on localhost and waits for incoming connections
* For each connected user creates Thread to get messages from them
* Connects to MySQL database on localhost to verify user information
### Client
* Writes messages to and recieves messages from all users in chat
