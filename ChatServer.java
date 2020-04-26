import java.io.*;
import java.net.*;

public class ChatServer {
	public static final int cs_port = 7777;
	public static final int cs_maxclient = 50;

	public static void main(String args[]) {
		try {
			ServerSocket server_socket = new ServerSocket(cs_port);
			System.out.println("서버소켓 실행 : 클라이언트의 접속을 기다립니다.");
			while (true) {
				Socket sock = null;
				ServerThread client = null;
				try {
					sock = server_socket.accept();
					client = new ServerThread(sock);
					client.start();
				} catch (IOException e) {
					System.out.println(e);
					try {
						if (sock != null)
							sock.close();
					} catch (IOException e1) {
						System.out.println(e1);
					} finally {
						sock = null;
					}
				}
			}
		} catch (IOException e) {
		}
	}
}
