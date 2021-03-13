package ch.wenkst.sw_utils.communication.tls;

import javax.net.ssl.SSLSocket;

import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.communication.tls.server.TlsServer;

public class TlsTestServer extends TlsServer {
	public TlsTestSession testSession;

	@Override
	protected ISession onNewConnection(TlsServer owner, SSLSocket socket) {
		testSession = new TlsTestSession();
		testSession.init(owner, socket, "tls-test-session");
		testSession.start();
		return testSession;
	}
	

	public TlsTestSession getTestSession() {
		return testSession;
	}
}
