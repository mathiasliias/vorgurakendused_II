package controller;


import data.DataManager;
import httpclient.HttpClient;
import httpserver.HttpServer;

public class AppController {
	
	private static HttpServer httpServer;
	private static HttpClient httpClient;
	public static DataManager dataManager = new DataManager();
	
	public static void main(String[] args) {
		
		try {
			if (args.length > 0) {
				httpServer = new HttpServer(dataManager, Integer.valueOf(args[0]));
			} else {
				httpServer = new HttpServer(dataManager);
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid port value: " + args[0]);
			httpServer = new HttpServer(dataManager);
		}
		
		httpClient = new HttpClient(dataManager);
		
		startServer();
		startClient();
		
		
	}
	
	public static void startServer() {
		Thread server = new Thread(httpServer);
		server.start();
	}
	
	public static void startClient() {
		Thread client = new Thread(httpClient);
		client.start();
	}
}
