package httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.Block;
import data.Peer;
import data.Transaction;


public class Requests {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	private BufferedReader br;
	Socket socket;
	
	//GET requests
	public List<Peer> getPeers(String ipAdress, int port) {
		
		String response = getRequest("GET /clones HTTP/1.1", ipAdress, port);
		try {
			return response == null ? null : objectMapper.readValue(response, new TypeReference<List<Peer>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Block> getAllBlocks(String ipAdress, int port) {
		return getBlocksFromLastBlock(ipAdress,  port, "");
	}

	public List<Block> getBlocksFromLastBlock(String ipAdress, int port, String fromBlock) {
		
		String response = getRequest("GET /blocks/" + fromBlock + " HTTP/1.1", ipAdress, port);
		System.out.println("Received response "+response);
		try {
			return response == null ? null : objectMapper.readValue(response, new TypeReference<List<Block>>(){});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Block getBlockWithId(String ipAdress, int port, String blockId) {
		
		String response = getRequest("GET /block/" + blockId + " HTTP/1.1", ipAdress, port);
		try {
			return response == null ? null : objectMapper.readValue(response, new TypeReference<List<Block>>(){});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private String getRequest(String request, String ipAdress, int port) {
		String response = null;
		try {
			socket = new Socket(InetAddress.getByName(ipAdress), port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println(request);
			pw.println("");
			pw.flush();

			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true) {
    			//Get to body
    			String headerLine = br.readLine();
    			if (headerLine.equals("\r\n") || headerLine.equals(""))
    				break;
    		}
			StringBuilder sBuilder = new StringBuilder();
			while((response = br.readLine()) != null) {
				sBuilder.append(response);
			}
			br.close();
			socket.close();
			return sBuilder.toString();
		} catch (IOException e) {
			if (e instanceof ConnectException) {
				System.out.println("Connection refused: " + ipAdress);
			}
		}
		return response;
	}

	//POST requests
	public void postBlockRequest(String ipAdress, int port, Block block) {
		try {
			String responseBody = "[" + objectMapper.writeValueAsString(block) + "]";
			socket = new Socket(InetAddress.getByName(ipAdress), port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println("POST /blocks" + " HTTP/1.1");
			pw.println("Content-Length: " + responseBody.getBytes().length);
			pw.println();
			pw.print(responseBody);
			pw.flush();

			System.out.println("Sent out block: " + block.blockId);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void postTransactionRequest(String ipAdress, int port, Transaction transaction) {
		try {
			String responseBody = "[" + objectMapper.writeValueAsString(transaction) + "]";
			System.out.println(responseBody);
			socket = new Socket(InetAddress.getByName(ipAdress), port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println("POST /transactions" + " HTTP/1.1");
			pw.println("Content-Type: application/json");
			pw.println("Content-Length: " + responseBody.getBytes().length);
			pw.println();
			pw.print(responseBody);
			pw.flush();

			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
