package ch.wenkst.sw_utils.communication.udp.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class UdpServer extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);
	
	protected int port;
	protected String serverName;
	private boolean isHealthy = true;
	
	protected DatagramSocket serverSocket = null;
	protected int readTimeout = 1000;
	protected byte[] inputBuffer = new byte[10000];
	
	
	/**
	 * initializes the udp server
	 * @param port 			port on which the udp server listens
	 * @param serverName 	the name to identify the server
	 */
	public void init(int port, String serverName) {
		this.port = port;
		this.serverName = serverName;	
		
		setName(serverName);
	}
	
	
	@Override
	public void doWork() {
		try {
			isHealthy = true;
			DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
			serverSocket.receive(packet);
			
			int len = packet.getLength();
			byte[] message = Arrays.copyOf(inputBuffer, len);
			processMessage(message, packet.getAddress(), packet.getPort());

		} catch (SocketTimeoutException ex) {

		} catch (Exception e) {
			logger.error("udp server socket error, terminate the server: ", e);
			stopServer();
		}
	}
	
	
	public void sendMessage(byte[] message, String host, int remotePort) {
		try {
			InetAddress address= InetAddress.getByName(host);
			DatagramPacket packet = new DatagramPacket(message, message.length, address, remotePort);
			serverSocket.send(packet);
		
		} catch (Exception e) {
			logger.error(serverName + ": error writing to the udp socket: ", e);
		}
	}
	
	
	/**
	 * process a received udp message
	 * @param message 		the udp message
	 * @param address 		the address of the connected client
	 * @param remotePort 	the remote client port
	 */
	protected abstract void processMessage(byte[] message, InetAddress address, int remotePort);
	
	
	@Override
	public void startWork() {
		boolean isOpen = openServer();
		if (!isOpen) {
			logger.error("failed to open the udp server on port " + port);
			stopServer();
		}
	}

	@Override
	public void terminateWork() {
		try {
			logger.info(serverName + " close the udp server socket, port: " + port);
			serverSocket.close();

		} catch (Exception e) {
			logger.error(serverName + ": error closing the udp-server socket: ", e);
		}
	}
	
	
	
	/**
	 * opens the udp server
	 * @return 		true if the server could be opened, false if an error occurred
	 */
	protected boolean openServer() {
		logger.info(serverName + ": open a udp server on port " + port);
		
		try {
			serverSocket = new DatagramSocket(port);
			serverSocket.setSoTimeout(readTimeout);
			logger.info(serverName + ": udp server socket listening on port " + port);
			return true;

		} catch (Exception e) {
			logger.error("error opening the udp server socket: ", e);
			return false;
		}
	}	
	
	/**
	 * starts the server
	 */
	public void startServer() {
		start();
	}
	

	/**
	 * stops the udp server
	 */
	public void stopServer() {		
		logger.info(serverName + ": stop the udp server, port: " + port);
		stopWorker();
	}
		
	
	public int getPort() {
		return port;
	}


	public boolean isHealthy() {
		return isHealthy;
	}


	public void setHealthy(boolean isHealthy) {
		this.isHealthy = isHealthy;
	}
}