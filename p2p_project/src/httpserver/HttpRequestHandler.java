package httpserver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.Block;
import data.DataManager;


public class HttpRequestHandler implements Runnable {
    public final static String CRLF = "\r\n";

    private Socket socket;

    private InputStream input;

    private static OutputStream output;

    private BufferedReader br;
    
    private String statusOkLine = "HTTP/1.0 200 OK" + CRLF;
    private String statusBadLine = "HTTP/1.0 400 Bad Request" + CRLF;
    private String contentTypeJSONLine = "Content-Type: application/json" + CRLF;
    private String contentLengthLine = "Content-Length: ";
    private DataManager dataManager;
        

    public HttpRequestHandler(Socket socket, DataManager dataManager) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(input));
        this.dataManager = dataManager;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void processRequest() throws Exception {
        while (!socket.isClosed()) {
        	String headerLine = br.readLine();
            
            if (headerLine.equals(CRLF) || headerLine.equals(""))
                break;

            // GET[/clones, /blocks, /blocks/x, /block/x,] POST[/block /transaction]
            String[] headerLines = headerLine.split("\\s");
            
            if (headerLines[0].equals("GET")) {
            	if (headerLines[1].equals("/clones")) {
            		sendAllPeers();
            	} else if (headerLines[1].equals("/blocks")) {
            		sendBlocks("0");
				} else if (Pattern.matches("/blocks/.*", headerLines[1])) {
					String blocksFrom = headerLines[1].split("/")[2];
					sendBlocks(blocksFrom);
				} else if (Pattern.matches("/block/.*", headerLines[1])) {
					String blockId = headerLines[1].split("/")[2];
					sendBlock(Integer.valueOf(blockId));
				}
            } else if (headerLines[0].equals("POST")) {
            	if (headerLines[1].equals("/transactions")) {
            		//Read body
            		while (true) {
            			//Get to body
            			headerLine = br.readLine();
            			if (headerLine.equals(CRLF) || headerLine.equals(""))
            				break;
            		}
                	StringBuilder requestBody = new StringBuilder();
                	br.lines().forEach(s->requestBody.append(s));
                	dataManager.addTransaction(requestBody.toString());
                	
            	} else if (headerLines[1].equals("/blocks")) {
            		while (true) {
            			headerLine = br.readLine();
            			if (headerLine.equals(CRLF) || headerLine.equals(""))
            				break;
            		}
                	StringBuilder requestBody = new StringBuilder();
                	br.lines().forEach(s->requestBody.append(s));
                	dataManager.addBlocks(requestBody.toString());
            	}
            }

        }
        if (!socket.getKeepAlive()) {
        	try {
        		output.close();
        		br.close();
        		socket.close();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
    }
    
    private void sendBlock(Integer valueOf) {
    	try {
    		System.out.println("Searching for block " + valueOf);
			writeBytesOk(dataManager.getBlock(valueOf));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendBlocks(String fromBlockId) {
    	try {
    		List<Block> blocks = dataManager.getBlocks(fromBlockId);
    		String json = null;
    		if (blocks != null) {
    			json = new ObjectMapper().writeValueAsString(dataManager.getBlocks(fromBlockId));
    		}
    		writeBytesOk(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private  void sendAllPeers() {
		try {
			String json = dataManager.getClones();
			writeBytesOk(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void writeBytesBad() throws IOException {
    	//Bad request
		output.write(statusBadLine.getBytes());
		System.out.println(statusBadLine);
	}

	private void writeBytesOk(String jsonBody) throws IOException {
		if (!socket.isClosed()) {
			if (jsonBody == null || jsonBody.length() <= 2) {
	    		writeBytesBad();
    	
	    	} else {
	    		// Send status OK line.
				output.write(statusOkLine.getBytes());
				System.out.println(statusOkLine);
				//Content length
				contentLengthLine += jsonBody.getBytes().length + CRLF;
				output.write(contentLengthLine.getBytes());
				//Content type
				output.write(contentTypeJSONLine.getBytes());
				System.out.println(contentLengthLine);
				//Empty line
				output.write(CRLF.getBytes());
				//Body
				output.write(jsonBody.getBytes());
				System.out.println(jsonBody);
	    	}
			
		} else {
			System.out.println("Socket already closed");
		}
    }
}
