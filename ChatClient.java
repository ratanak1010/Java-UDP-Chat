import java.io.*;
import java.net.*;
import java.util.*;

class Chatclient extends Thread {
	static ArrayList<PrintWriter> list = new ArrayList<PrintWriter>();
	Socket socket;
	PrintWriter writer;

	Chatclient(Socket socket) {
		this.socket = socket;
		try {
			writer = new PrintWriter(socket.getOutputStream());
			list.add(writer);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true) {
				String str = reader.readLine();
				if (str == null)
					break;
				for (PrintWriter writer : list) {
					writer.println(str);
					writer.flush();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			list.remove(writer);
			try {
				socket.close();
			} catch (Exception ignored) {
			}
		}
	}
}
