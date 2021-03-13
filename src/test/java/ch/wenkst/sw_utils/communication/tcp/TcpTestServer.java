package ch.wenkst.sw_utils.communication.tcp;

import java.net.Socket;

import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.communication.tcp.server.TcpServer;

public class TcpTestServer extends TcpServer {
	public TcpTestSession testSession;

	@Override
	protected ISession onNewConnection(TcpServer owner, Socket socket) {
		testSession = new TcpTestSession();
		testSession.init(owner, socket, "tcp-test-session");
		testSession.start();
		return testSession;
	}

	public TcpTestSession getTestSession() {
		return testSession;
	}
}
