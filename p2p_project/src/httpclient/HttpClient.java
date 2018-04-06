package httpclient;

import java.util.ArrayList;
import java.util.List;


import data.Block;
import data.DataManager;
import data.Peer;

public class HttpClient implements Runnable {
	
	private DataManager dataManager;
	
	private static final Requests requests = new Requests();
	
	public HttpClient(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void run() {
		try {
			runHttpClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void runHttpClient() {
		//Client infinite loop
		while (true) {
			//Add peers 
			List<Peer> peers = dataManager.getPeers();
			if (peers.size() < 10) {
				getPeers(peers);
			}
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Get more blocks
			int N = dataManager.getBlockN();
			getBlocks(String.valueOf(N));
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Send out blocks
			List<Block> newBlocks = dataManager.getBlocksToSend();
			if (newBlocks.size() > 0) {
				for (Block block: newBlocks) {
					sendOutBlock(block);
				}
				dataManager.clearBlocksToSend();
			}
		}
	}

	private void getPeers(List<Peer> peers) {
		
		List<Peer> addPeers = new ArrayList<>();
		for (Peer peer: peers) {
			List<Peer> newPeers = requests.getPeers(peer.peerIp, peer.port);
			if (newPeers != null && !newPeers.isEmpty()) {
				for (Peer p: newPeers) {
					if (!peers.contains(p)) {
						addPeers.add(p);
					}
				}
			}
		}
		dataManager.addPeers(addPeers);
	}

	public void getBlocks(String fromBlock) {
		List<Peer> peers = dataManager.getPeers();
		for (Peer peer: peers) {
			System.out.println("Asking blocks from number: " + fromBlock);
			List<Block> blocks = requests.getBlocksFromLastBlock(peer.peerIp, peer.port, fromBlock);
			if (blocks != null && !blocks.isEmpty()) {
				System.out.println("added blocks: "+blocks.size());
				dataManager.addBlocks(blocks);
				break;
			}
		}
	}

	public void sendOutBlock(Block block) {
		List<Peer> peers = dataManager.getPeers();
		for (Peer peer: peers) {
			requests.postBlockRequest(peer.peerIp, peer.port, block);
		}
	}
}
