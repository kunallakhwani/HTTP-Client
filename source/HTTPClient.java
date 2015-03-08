import java.io.BufferedReader;
public class HTTPClient {

	public static void main(String[] args) {
		BufferedReader in = null;
		//InputStreamReader in = null;
		int status=0;
		String message = null;
		TcpConn tcpconn = new TcpConn();
		
		message = tcpconn.connection(args[0], args[1], -1);
		tcpconn.sendMessage(message);
		in = tcpconn.receiveResponse();
		status = tcpconn.displayResponse(args[0], in);
		tcpconn.closeConnection();
		tcpconn.checkStatus(status);
	}

}