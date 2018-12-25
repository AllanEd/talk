package com.app.talk;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import com.app.talk.client.command.set.MessageCommand;
import com.app.talk.communication.Communicator;
import com.app.talk.communication.CommunicatorFactory;

/**
 * The dispatcher waits for clients to connect to its serverSocket and creates a communicator for each connected client.
 */
public class Dispatcher implements Runnable {
	private ServerSocket server;
    private int port;
	/**
	 * stores chat clients, represented by communicator objects.
	 */
	static ArrayList<Communicator> clientList = new ArrayList<Communicator>();
    private static boolean acceptClients = true;

    /**
     * Dispatcher constructor.
     *
     * @param port The port to listen to.
     */
    Dispatcher(int port) {
        this.port = port;
    } //constructor

    /**
     * Runnable implementation.
     */
    public void run() {
        try {
            this.listen();
        } catch (IOException | ClassNotFoundException e) {
        	e.printStackTrace();
        } //try-catch
    } //run

    /**
     * Creates communicators for connected clients.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void listen() throws IOException, ClassNotFoundException {
        server = new ServerSocket(this.port);
        System.out.println("Server started. Listening for incoming connection requests on port: " + this.port);   

        while (acceptClients) {     	
        	try{
        		Socket client = server.accept();
                System.out.println("Connection request from " + client.getInetAddress().toString() + ":" + client.getPort());
                Dispatcher.addClient(CommunicatorFactory.getInstance().createCommunicator(client));
        	} catch (SocketException e){
        		//this is fine
        	}
        } //while
    } //listen

	public void close() {
		try {
			acceptClients = false;
			server.close();
		} catch (IOException e) {
			// Doesn't matter if already closed.			
		}		
	}//close()

	/**
	 * sends a received message to all known chat clients.
	 * @param message textual message to be sent.
	 * @throws IOException 
	 */
	synchronized public static void broadcast(String message) {
		int counter = 0;
		System.out.println("Message: \"" + message + "\" received.");
		for (Communicator communicator : Dispatcher.clientList) {
			try {
				communicator.getSender().send(new MessageCommand(message));
				System.out.println(" -> redirect to client " + counter++);    			
			} catch (Exception e) {
				e.printStackTrace();
			} //try-catch
		} //for
	} //broadcast

	/**
	 * removes a specific chat client from the list of clients.
	 * @param client communicator object representing the specific chat client.
	 */
	synchronized public static void removeClient(Communicator client) {
		boolean removed = Dispatcher.clientList.remove(client);
		if (removed) {
			client.close();
		}
		if (Dispatcher.clientList.size() == 0) {
			System.out.println("No more clients available - shutting down server.");
			TalkServer.dispatcher.close();
		}
	}

	/**
	 * adds a chat client to the list of clients.
	 * @param client communicator object representing the specific chat client.
	 */
	synchronized public static void addClient(Communicator client) {
		Dispatcher.clientList.add(client);
	}
} //Dispatcher Class
