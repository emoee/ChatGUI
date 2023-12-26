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
	private ArrayList <ChatSocketListener> sListeners = new ArrayList<ChatSocketListener>();
	private JFrame chatWindow;
	ChatClient() {
		id = new java.rmi.server.UID().toString();
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		ChatPanel chatPanel = new ChatPanel(this);
		chatPanel.setBorder(BorderFactory.createEtchedBorder());
		StatusBar status = StatusBar.getStatusBar();
		status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(1, 2, 2, 2)));
		contentPane.add(status, BorderLayout.SOUTH);
		ChatMessageReceiver chatReceiver = new ChatMessageReceiver(this);
		// 占쎄깻占쎌뵬占쎌뵠占쎈섧占쎈뱜�몴占� 筌띲끆而삭퉪占쏙옙�땾嚥∽옙 占쎌읈占쎈뼎占쎈퉸 占쎄문占쎄쉐占쎌쁽 占쎌깈�빊占�
		chatReceiver.setMessageReceiver(chatPanel);
	//test lih
		chatWindow = new JFrame("Minimal Chat - Concept Proof");
		contentPane.add(chatPanel); 
		//wow
		
		chatWindow.setContentPane(contentPane);
		chatWindow.setSize(450, 350);
		
		chatWindow.setLocationRelativeTo(null);
		chatWindow.setVisible(true);
		chatWindow.addWindowListener(this);
		
		this.addChatSocketListener(chatPanel);
		this.addChatSocketListener(chatReceiver);
		try {
			P2P.getInstance().startService();
			// P2P 占쎄깻占쎌뵬占쎌뵠占쎈섧占쎈뱜筌띾뜄�뼄 占쎈릭占쎄돌占쎈뎃 占쎈꺖�노낀�궢 占쎈뮞占쎌쟿占쎈굡筌띾슢諭얏묾占�
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean connect() {
		if(socketAvailable()) return true;
		chatName = JOptionPane.showInputDialog(chatWindow, "Enter chat name:");
		if(chatName == null ) return false;

		try {
			socket = new Socket("210.125.213.7", 1223);
			for(ChatSocketListener lsnr: sListeners) {
				lsnr.socketConnected(socket);
			}
			return true;
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to connect chat server", "Eror", JOptionPane.ERROR_MESSAGE);
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
		for(ChatSocketListener lsnr: sListeners) {
			lsnr.socketClosed();
		}
	}
	@Override
	public String getName() {
		return  chatName;
	}
	@Override
	public String getId() {
		return id;
	}
	
	public void addChatSocketListener(ChatSocketListener lsnr) {
		sListeners.add(lsnr);
	}
	public void removeChatSocketListener(ChatSocketListener lsnr) {
		sListeners.remove(lsnr);
	}
	public void windowClosing(WindowEvent e) {
		disConnect();
		System.exit(0);
	}
	public static void main(String [] args) throws Exception {
		new ChatClient();
	}
}