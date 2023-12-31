package lect.chat.client;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import lect.chat.client.event.*;
import lect.chat.client.p2p.P2P;
public class ChatClient extends WindowAdapter implements ChatConnector {
	private Socket socket;
	private String chatName;
	private String id;
	private ArrayList <ChatSocketListener> socketListeners = new ArrayList<ChatSocketListener>();
	private JFrame chatWindow;
	private ConnectButton connectDisconnect; 

	ChatClient() {
		id = new java.rmi.server.UID().toString();
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		ChatPanel chatPanel = new ChatPanel(this);

		chatPanel.setBorder(BorderFactory.createEtchedBorder());
		ChatMessageReceiver messageReceiver = new ChatMessageReceiver(this);
		messageReceiver.setMessageReceiver(chatPanel);
	        
		chatWindow = new JFrame("카카오스쿨톡");
		contentPanel.add(chatPanel);

		chatWindow.setContentPane(contentPanel);
		chatWindow.setSize(530, 380);

		chatWindow.setLocationRelativeTo(null);
		chatWindow.setVisible(true);
		chatWindow.addWindowListener(this);

		this.addChatSocketListener(chatPanel);
		this.addChatSocketListener(messageReceiver);
		try {
			P2P.getInstance().startService();
			// P2P �겢�씪�씠�뼵�듃留덈떎 �븯�굹�뵫 �냼耳볤낵 �뒪�젅�뱶留뚮뱾湲�
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean connect(String ipAddress, int port) {
		if (socketAvailable()) return true;
		
		chatName = JOptionPane.showInputDialog(chatWindow, "채팅 닉네임을 입력하세요");
        
        while (chatName == null || chatName.trim().isEmpty()) { // null이 아닐 때까지 입력을 받는다
            JOptionPane.showMessageDialog(null, "채팅 닉네임이 입력되지 않았습니다.", "에러", JOptionPane.ERROR_MESSAGE);
        	chatName = JOptionPane.showInputDialog(chatWindow, "채팅 닉네임을 입력하세요");
        }
        
        try {
            socket = new Socket(ipAddress, port);

            for (ChatSocketListener socketListener : socketListeners) {
                socketListener.socketConnected(socket);
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "채팅 서버에 연결 실패", "에러", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

	@Override
	public void disConnect() {
		if(socketAvailable()) {
			try {
				socket.close();
			} catch(IOException ex) {
			}
		}
	}
	@Override
	public Socket getSocket() {
		return socket;
	}
	@Override 
	public boolean socketAvailable() {
		return !(socket == null || socket.isClosed());
	}
	@Override
	public void invalidateSocket() {
		disConnect();
		for(ChatSocketListener socketListener: socketListeners) {
			socketListener.socketClosed();
		}
	}
	@Override
	public String getName() {
		return chatName;
	}
	@Override
	public String getId() {
		return id;
	}

	public void addChatSocketListener(ChatSocketListener socketListener) {
		socketListeners.add(socketListener);
	}
	public void removeChatSocketListener(ChatSocketListener socketListener) {
		socketListeners.remove(socketListener);
	}
	public void windowClosing(WindowEvent e) {
		disConnect();
		System.exit(0);
	}
	public static void main(String [] args) throws Exception {
		new ChatClient();
	}
}