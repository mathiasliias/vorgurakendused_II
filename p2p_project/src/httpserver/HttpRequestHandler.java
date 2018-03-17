package httpserver;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class HttpRequestHandler implements Runnable {
    final static String CRLF = "\r\n";

    private Socket socket;

    private InputStream input;

    private OutputStream output;

    private BufferedReader br;
    
    private String statusOkLine = "HTTP/1.0 200 OK" + CRLF;;
    private String contentTypeJSONLine = "Content-Type: application/json" + CRLF;
    private String entityBody = null;
    private String contentLengthLine;
        

    public HttpRequestHandler(Socket socket) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(input));
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void processRequest() throws Exception {
        while (true) {

            String headerLine = br.readLine();
            System.out.println(headerLine);
            if (headerLine.equals(CRLF) || headerLine.equals(""))
                break;

            // GET[/clones, /blocks, /blocks/x, /block/x,] POST[/block /transaction]
            String[] headerLines = headerLine.split("\\s");
            

            if (headerLines[0].equals("GET")) {
            	
            	if (headerLines[1].equals("/clones")) {
            		sendOkResponse();
            	} else if (headerLines[1].equals("/blocks")) {
            		sendOkResponse();
				} else if (Pattern.matches("/blocks/.*", headerLines[1])) {
					String blocksFrom = headerLines[1].split("/")[1];
					sendOkResponse();
				} else if (Pattern.matches("/block/.*", headerLines[1])) {
					String blockId = headerLines[1].split("/")[1];
					sendOkResponse();
				}
            	

                String fileName = "/results.txt";

                FileInputStream fis = null;
                boolean fileExists = true;
                try {
                    fis = new FileInputStream("." + fileName);
                } catch (FileNotFoundException e) {
                    fileExists = false;
                }

                if (fileExists) {

                } else {
                    String statusBadLine = "HTTP/1.0 404 Not Found" + CRLF;
                    entityBody = "<HTML>"
                            + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>"
                            + "<BODY>404 Not Found"
                            + "<br>usage:http://" + socket.getLocalAddress() + ":" + socket.getLocalPort()
                            + headerLines[1] +"</BODY></HTML>";
                    contentLengthLine = "Content-Length: " + (new Integer(entityBody.getBytes().length)).toString() + CRLF;
                }

                // Send the entity body.
                if (fileExists) {
                    sendBytes(fis, output);
                    fis.close();
                } else {
                    output.write(entityBody.getBytes());
                }

            }
            
            if (headerLines[0].equals("POST")) {
            	if (headerLines[1].equals("/transactions")) {
            		
            	} else if (headerLines[1].equals("/blocks")) {
            		
				}
            }

        }

        try {
            output.close();
            br.close();
            socket.close();
        } catch (Exception e) {
        }
    }
    
    private  void sendOkResponse() {
    	try {
            FileInputStream fis = new FileInputStream("peers.txt");
            contentLengthLine = "Content-Length: "
                + (new Integer(fis.available())).toString() + CRLF;
            // Send the status line.
			output.write(statusOkLine.getBytes());
			System.out.println(statusOkLine);
			
			// Send the content type line.
			output.write(contentTypeJSONLine.getBytes());
			System.out.println(contentTypeJSONLine);
			
			// Send the Content-Length
			output.write(contentLengthLine.getBytes());
			System.out.println(contentLengthLine);
			
			// Send a blank line to indicate the end of the header lines.
			output.write(CRLF.getBytes());
			System.out.println(CRLF);
			
			sendBytes(fis, output);
			fis.close();
        } catch (IOException e) {
            //TODO
        }
    }
    

    private void sendBytes(FileInputStream fis, OutputStream os)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")
                || fileName.endsWith(".txt")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else {
            return "application/octet-stream";
        }
    }
}
