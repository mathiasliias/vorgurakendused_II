package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DataManager {

	private static Path path = Paths.get("conf.txt");
	
	private static List<Peer> peers = new ArrayList<>();
	
	
	public List<Peer> getAllNodes() {
		return peers;
	}
	
	public static JSONObject readPeersFromFile() {
		StringBuilder data = new StringBuilder();
	    Stream<String> lines;
		try {
			lines = Files.lines(path);
			lines.forEach(line -> data.append(line).append("\n"));
			lines.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		JSONObject jsonPeers = null;
		try {
			jsonPeers = new JSONObject(data.toString()/*"{\"phonetype\":\"N95\",\"cat\":\"WP\"}"*/);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return jsonPeers;
	}
}
