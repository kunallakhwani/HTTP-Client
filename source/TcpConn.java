import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
public class TcpConn {
		Socket s = null;
		PrintWriter out = null; 
		BufferedReader in = null;
		InetAddress[] addr = null;
		int redirect = 1;
		String message = null;
		public String connection(String method, String link, int count) {
			s = new Socket();
			String url = link;
			URI uri = null;
			String hostname = null;
			String path = null;
			int port = 0;
			if (url.startsWith("https://")) {
				System.out.println("This HTTP Client does not handle HTTPS requests.");
				System.exit(1);
			}
			if (!url.startsWith("http://")) {
				url = "http://" + url;
			}
			
			try {
				uri = new URI(url);
				hostname = uri.getHost();
				path = uri.getPath(); 
				port = uri.getPort();
				if(path.equals("")) {
					path = "/";
				}
				if(port == -1) {
					port = 80;
				}
				addr = InetAddress.getAllByName(hostname);
				for(count++; count<addr.length; count++) {
		
					s.connect(new InetSocketAddress(addr[count], port));
	
					if(s.isConnected()) {
					break;
					}
					else if(count == addr.length-1 && !s.isConnected()) {
						System.out.println("Unable to connect.");
						System.exit(1);
					}
				}
				
			} catch (UnknownHostException e) {
				System.out.println("Unknown Host.");
				System.exit(1);
			} catch (URISyntaxException e1) {
				System.out.println("Incorrect URL syntax.");
				System.exit(1);
			} catch (IOException e) {
				connection(method, url, count);
			}
			
			message = method + " " + path + " HTTP/1.1\nHost: " + hostname + "\nConnection: close" + "\r\n\r\n";
			
			return message;
		}
		public void sendMessage(String message) {
			try {
				out = new PrintWriter(s.getOutputStream(), true);
				out.println(message);
				out.flush();
			} catch (IOException e) {
				System.out.println("Unable to send message.");
				System.exit(1);
			}
		}
		
		public BufferedReader receiveResponse() {
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream(), "cp1256"));
			} catch (IOException e) {
				System.out.println("Unable to receive message.");
				System.exit(1);
			}
			return in;
		}
		
		public int displayResponse(String method, BufferedReader in) {
			String response="";
			String fullResponse="";
			int status=0;
			int status_code=0;
			int flag=0;
			int isChunked = 0;
			try {
				while((response = in.readLine()) != null && redirect <=6){

					if(response.contains("HTTP/1.1")) {
						status = Integer.parseInt(response.substring(9, 12));
						status_code = Integer.parseInt(response.substring(9, 10));
					}
					
					if((status == 301 || status == 302 || status == 303 || status == 307) && (response.contains("Location")) && redirect <=6) {
						closeConnection();
						String link = response.substring(10);
						redirect++;
						System.out.println("Redirecting to " + link);
						message = connection(method, link, -1);
						sendMessage(message);
						in = receiveResponse();
						displayResponse(method, in);
					}
					
					if(response.contains("Transfer-Encoding: chunked")) {
						isChunked=1;
					}
					if ((response.trim().equals("")))
						flag=1;
					
					if (flag==1 && isChunked==1) {
						String hex = in.readLine();
						while (!hex.trim().equals("0")) {
						long chunkSize = Long.parseLong(hex, 16);
						for(int i =0; i <= chunkSize; i++){
							System.out.print((char)in.read());
						}
						in.readLine();
						hex = in.readLine();
						}
						break;
					}

					if(flag == 1 && !(response.trim().equals(""))) {
						
						if (fullResponse.equals(""))
							fullResponse = response;
						else
							fullResponse = fullResponse + "\n" + response;
						
				}
				}
				if(!fullResponse.equals(""))
				System.out.print(fullResponse);

				if (redirect > 6) {
					System.out.println("Unable to connect.");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("No response from server.");
				System.exit(1);
			}
			return status_code;
		}
		
		public void checkStatus(int status_code){
			
			if(status_code == 2)
				System.exit(0);
			else if(status_code == 3)
				System.exit(3);
			else if(status_code == 4)
				System.exit(4);
			else if(status_code == 5)
				System.exit(5);
			else
				System.exit(1);
		}
		
		public void closeConnection() {
			try {
				out.close();
				in.close();
				s.close();
			} catch (IOException e) {
				System.out.println("Error in closing socket.");
				System.exit(1);
			}
		}
}