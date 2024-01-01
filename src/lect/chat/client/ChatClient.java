
//test
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
		chatReceiver.setMessageReceiver(chatPanel);
	
		chatWindow = new JFrame("Minimal Chat - Concept Proof");
		contentPane.add(chatPanel);
		
		chatWindow.setContentPane(contentPane);
		chatWindow.setSize(450, 350);
		
		chatWindow.setLocationRelativeTo(null);
		chatWindow.setVisible(true);
		chatWindow.addWindowListener(this);
		
		this.addChatSocketListener(chatPanel);
		this.addChatSocketListener(chatReceiver);
		try {
			P2P.getInstance().startService();
			// P2P �겢�씪�씠�뼵�듃留덈떎 �븯�굹�뵫 �냼耳볤낵 �뒪�젅�뱶留뚮뱾湲�
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