package ch.wenkst.sw_utils.messaging.zero_mq;

public class BrokerConfigZMQ {
	private String frontendHost;
	private int frontendPort;
	private String frontendProtocol;
	private String backendHost;
	private int backendPort;
	private String backendProtocol;
	
	
	public BrokerConfigZMQ frontendHost(String frontendHost) {
		this.frontendHost = frontendHost;
		return this;
	}
	
	public BrokerConfigZMQ frontendPort(int frontendPort) {
		this.frontendPort = frontendPort;
		return this;
	}
	
	public BrokerConfigZMQ frontendProtocol(String frontendProtocol) {
		this.frontendProtocol = frontendProtocol;
		return this;
	}
	
	public BrokerConfigZMQ backendHost(String backendHost) {
		this.backendHost = backendHost;
		return this;
	}
	
	public BrokerConfigZMQ backendPort(int backendPort) {
		this.backendPort = backendPort;
		return this;
	}
	
	public BrokerConfigZMQ backendProtocol(String backendProtocol) {
		this.backendProtocol = backendProtocol;
		return this;
	}

	
	
	public String getFrontendHost() {
		return frontendHost;
	}

	public int getFrontendPort() {
		return frontendPort;
	}

	public String getFrontendProtocol() {
		return frontendProtocol;
	}

	public String getBackendHost() {
		return backendHost;
	}

	public int getBackendPort() {
		return backendPort;
	}

	public String getBackendProtocol() {
		return backendProtocol;
	}
}
