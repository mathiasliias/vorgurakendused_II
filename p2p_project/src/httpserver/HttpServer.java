package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String args[]) {
        new HttpServer().startHttpServer(args);
    }

    private void startHttpServer(String[] args) {
        int port;
        ServerSocket server_socket;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            port = 1500;
        }
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
                    HttpRequestHandler request = new HttpRequestHandler(socket);
                    // Create a new thread to process the request.
                    Thread thread = new Thread(request);

                    // Start the thread.
                    thread.start();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

