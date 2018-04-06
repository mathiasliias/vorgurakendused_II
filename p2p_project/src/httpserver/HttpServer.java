package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import data.DataManager;

public class HttpServer implements Runnable {

	private int port = 1500;
	private DataManager dataManager;
	
	public HttpServer(DataManager dataManager, int port) {
		this.port = port;
		this.dataManager = dataManager;
	}
	
	public HttpServer(DataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	@Override
	public void run() {
		runHttpServer();
	}


    private void runHttpServer() {
        ServerSocket server_socket;
        try {

            server_socket = new ServerSocket(port);
            System.out.println("HttpServer running on port "
                    + server_socket.getLocalPort());
            
            // server infinite loop
            while (true) {
                Socket socket = server_socket.accept();
                System.out.println("New connection accepted "
                        + socket.getInetAddress() + ":" + socket.getPort());

                // Construct handler to process the HTTP request message.
                try {
                    HttpRequestHandler requestHandler = new HttpRequestHandler(socket, dataManager);
                    // Create a new thread to process the request.
                    Thread thread = new Thread(requestHandler);

                    // Start the thread.
                    thread.start();
                } catch (Exception e) {
                    System.out.println("thread error" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

