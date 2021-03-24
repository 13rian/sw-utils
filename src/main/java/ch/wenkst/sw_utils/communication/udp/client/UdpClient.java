package ch.wenkst.sw_utils.communication.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class UdpClient extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
				
	protected String host;
	protected int port;
	protected String clientName;

	protected DatagramSocket socket = null;
	protected InetAddress address = null;
	protected int socketTimeout = 2000;
	protected byte[] inputBuffer = new byte[10000];
	
	
	/**
	 * handles the client side of the udp connection
	 */
	public UdpClient() {	
		
	}
	
	
	/**
	 * initializes the udp client
	 * @param host 			host to connect to
	 * @param port 			server to connect to 
	 * @param clientName 	arbitrary name to identify the client
	 */
	public void init(String host, int port, String clientName) {
		this.host = host;
		this.port = port;
		this.clientName = clientName;
	}
	
	
	@Override
	public void doWork() {
		try {			
			DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
			socket.receive(packet);
			
			int len = packet.getLength();
			byte[] message = Arrays.copyOf(inputBuffer, len);
			processMessage(message);

		} catch (SocketTimeoutException ex) {

		} catch (Exception e) {
			logger.error(clientName + ": error reading from udp socket: ", e);
			stopWorker();
		}			
	}
	
	
	/**
	 * processes an incoming message form the server
	 */
	protected abstract void processMessage(byte[] message);
	
	
	
	/**
	 * creates the udp client socket of the proxy
	 * @return	 	true if successfully opened, false otherwise
	 */
	public boolean connect() {
		logger.info(clientName +  ": open a udp socket to host: " + host + ", " + port);
		
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName(host);
			socket.setSoTimeout(socketTimeout);
			return true;

		} catch (Exception e) {
			logger.error(clientName + ": error opening the udp client socket: ", e);
			disconnect();
			return false;
		}
	}
	
	
	/**
	 * closes the udp client socket
	 */
	public void disconnect() {
		logger.info(clientName + ": close the socket");
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) { }
			socket = null;
		}
	}
	
	
	/**
	 * starts the udp client
	 */
	public void startClient() {
		start();
	}
	
	
	/**
	 * stops the tcp client
	 */
	public void stopClient() {
		stopWorker();
	}

	
	/**
	 * sends the passed message to the server
	 * @param message 	bytes to send to the server
	 */
	public void sendMessage(byte[] message) {
		try {
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
			socket.send(packet);
		
		} catch (Exception e) {
			logger.error(clientName + ": error writing to the udp socket: ", e);
		}
	}


	public DatagramSocket getSocket() {
		return socket;
	}
}
