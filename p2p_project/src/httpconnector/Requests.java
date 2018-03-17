package httpconnector;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import data.Block;
import data.Peer;


public class Requests {
	
	public static void main(String[] args) {
		new Requests().askForPeers("http://localhost:1500");
	}
	
	private final Client client = ClientBuilder.newClient();

	public void askForPeers(String targetUrl) {
		WebTarget peer = client.target(targetUrl + "/clones");
		Response response = peer.request().get();
		List<Peer> peers = response.readEntity(new GenericType<List<Peer>> () {});
		peers.stream().forEach(p->System.out.println(p.peerId + p.peerIp));
		//save peers to List
	}
	
	public void askForAllBlocks(String targetUrl) {
		WebTarget peer = client.target(targetUrl + "/blocks");
		Response response = peer.request().get();
		List<Block> blocks = response.readEntity(new GenericType<List<Block>> () {});
		blocks.stream().forEach(p->System.out.println(p.blockId + p.blockHash));
		//save peers to List
	}
	
	public void askForBlocksFromLastBlock(String targetUrl, String lastBlock) {
		WebTarget peer = client.target(targetUrl + "/blocks/" + lastBlock);
		Response response = peer.request().get();
		List<Block> blocks = response.readEntity(new GenericType<List<Block>> () {});
		blocks.stream().forEach(p->System.out.println(p.blockId + p.blockHash));
		//save peers to List
	}
	
	public void askForBlockWithId(String targetUrl, String blockId) {
		WebTarget peer = client.target(targetUrl + "/block/" + blockId);
		Response response = peer.request().get();
		Block block = response.readEntity(Block.class);
		block.transactions.stream().forEach(p->System.out.println(p.to + p.from));
		//save peers to List
	}
	
}
