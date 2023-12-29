package lect.chat.client;
import java.io.*;
import java.net.*;
import lect.chat.client.event.*;
import lect.chat.client.event.MessageReceiver;
public class ChatMessageReceiver implements Runnable, ChatSocketListener {
	private BufferedReader reader;
	private MessageReceiver receiver;
	ChatConnector connector;
	public ChatMessageReceiver(ChatConnector c) {
		connector = c;
		// 커넥터는 클라이언트임
<<<<<<< HEAD
		// test
		
=======
		// minsun
>>>>>>> 3cc0d88f1b4785bc4aa1fda187befabf1f2d3d9f
	}
	public void setMessageReceiver(MessageReceiver r) {
		receiver = r;
	}
	public void run() {
		String msg;
		try {
			while(connector.socketAvailable()) {
				// 클라이언트 소켓 사용 가능하면 메세지 한줄씩 읽기
				msg = reader.readLine();
				if(msg == null) {
					System.out.println("Terminating ChatMessageReceiver: message received is null");
					break;
				}
				// 메세지 없으면 문구 출력
				if(receiver != null) receiver.messageArrived(msg);
				// 메세지 있으면 출력 chatPanel에 구현되어있음
			}
		} catch(IOException e) {
			System.out.println("Terminating ChatMessageReceiver: " + e.getMessage());
		} finally  {
			connector.invalidateSocket();
			// 소켓 닫기
		}
		
	}
	
	public void socketClosed() { }
	public void socketConnected(Socket s) throws IOException {
		reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		new Thread(this).start();
		// 소켓에 버퍼 연결해 스레드 생성후 호출
	}
}
