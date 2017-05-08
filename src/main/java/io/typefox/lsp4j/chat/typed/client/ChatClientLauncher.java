package io.typefox.lsp4j.chat.typed.client;

import java.net.Socket;

import io.typefox.lsp4j.chat.typed.shared.ChatServer;
import io.typefox.lsp4j.chat.typed.shared.SocketLauncher;

public class ChatClientLauncher {

	public static void main(String[] args) throws Exception {
		ChatClientImpl chatClient = new ChatClientImpl();

		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		try (Socket socket = new Socket(host, port)) {
			SocketLauncher<ChatServer> launcher = new SocketLauncher<>(socket, chatClient, ChatServer.class);
			launcher.startListening().thenRun(() -> System.exit(0));
			chatClient.start(launcher.getRemoteProxy());
		}
	}

}