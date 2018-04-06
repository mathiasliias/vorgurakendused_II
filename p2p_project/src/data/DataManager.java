package data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DataManager {

	//Locks
	//Reading Blocks from file
	private final Object BLOCKS_LOCK = new Object();
	
	private final Object PEERS_LOCK = new Object();
	private final Object BLOCK_LOCK = new Object();
	private final Object CONFIG_LOCK = new Object();
	private final Object TRANSACTION_LOCK = new Object();
	
	private final List<Transaction> transactionsToSend = new ArrayList<>();
	private final List<Block> blocksToSend = new ArrayList<>();
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String PEERS = "peers.txt";
	private static final String CONFIG = "config.txt";
	
	private static final List<Peer> peers = readPeersFromFile();
	private static final Config CONF = readConfigFile();
	private static Block block;
	
	
	public static List<Peer> readPeersFromFile() {
		
		BufferedReader bReader;
		
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(PEERS)));
			StringBuilder sBuilder = new StringBuilder();
			bReader.lines().forEach(s->sBuilder.append(s));
			
			bReader.close();
			return OBJECT_MAPPER.readValue(sBuilder.toString(), new TypeReference<List<Peer>>(){});
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public static Config readConfigFile() {
		BufferedReader bReader;
		Config config = null;
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG)));
			StringBuilder sBuilder = new StringBuilder();
			bReader.lines().forEach(s->sBuilder.append(s));
			
			bReader.close();
			config = OBJECT_MAPPER.readValue(sBuilder.toString(), Config.class);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		//Set last block
//		if (config.unfinishedBlockId > config.lastBlockId) {
//			try {
//				bReader = new BufferedReader(new InputStreamReader(new FileInputStream("BLOCK_" + config.unfinishedBlockId + ".txt")));
//				StringBuilder sBuilder = new StringBuilder();
//				bReader.lines().forEach(s->sBuilder.append(s));
//				
//				bReader.close();
//				block = OBJECT_MAPPER.readValue(sBuilder.toString(), Block.class);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			block = new Block();
//			block.transactions = new ArrayList<>();
//			config.unfinishedBlockId = config.lastBlockId + 1;
//		}
		block = new Block();
		block.transactions = new ArrayList<>();
		config.unfinishedBlockId = config.lastBlockId;
		block.blockId = config.unfinishedBlockId+1;
		return config;
	}

	public String getClones() {

		synchronized (PEERS_LOCK) {
			try {
				return OBJECT_MAPPER.writeValueAsString(peers);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<Block> getBlocks(String from) {
		int compare = CONF.lastBlockId;
		if (Integer.valueOf(from) > compare) {
			return null;
		} else {
			List<Block> blocks = new ArrayList<>();

			synchronized (BLOCKS_LOCK) {
				for (int i = Integer.valueOf(from); i <= compare; i++) {
					try{
						BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream("BLOCK_" + i + ".txt")));
						StringBuilder sBuilder = new StringBuilder();
						bReader.lines().forEach(s->sBuilder.append(s));
						blocks.addAll(OBJECT_MAPPER.readValue(sBuilder.toString(), new TypeReference<List<Block>>(){}));
						bReader.close();
						System.out.println(sBuilder.toString());
					} catch (IOException e) {
						return null;
					}
				}
				System.out.println(blocks.size() + " size");
				return blocks;
			}
		}
	
	}

	public String getBlock(Integer valueOf) {
		
		int compare;
		synchronized (CONFIG_LOCK) {
			compare = CONF.lastBlockId;
		}
		if (valueOf > compare) {
			System.out.println("No block found with id: " + valueOf);
			return null;
		} else {
			StringBuilder sBuilder = new StringBuilder();
			synchronized (BLOCKS_LOCK) {
				try(BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream("BLOCK_" + valueOf + ".txt")))) {
					bReader.lines().forEach(s->sBuilder.append(s));
					bReader.close();
					return sBuilder.toString();
				} catch (IOException e) {
					return null;
				}
			}
		}
	}

	public void addTransaction(String json) {
		List<Transaction> transactions = null;
		try {
			transactions = OBJECT_MAPPER.readValue(json, new TypeReference<List<Transaction>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(json);
		for (Transaction transaction: transactions) {
			Transaction copyTransaction = copyTransaction(transaction);
			
			copyTransaction.transactionHash = getHash(copyTransaction.to + copyTransaction.from +
				copyTransaction.date + copyTransaction.amount);
			
			boolean duplicate = false;
			
			for (Transaction tran: block.transactions) {

				if (tran.transactionHash.equals(copyTransaction.transactionHash)) {
					System.out.println("Got duplicate transaction with hash: " + copyTransaction.transactionHash);
					duplicate = true;
					break;
				}
			}
			
			if (!duplicate) {
				System.out.println("adding transaction");
				//Add transaction
				synchronized (BLOCK_LOCK) {
					block.transactions.add(copyTransaction);
					System.out.println("Received new transaction with hash: " + copyTransaction.transactionHash);
					synchronized (TRANSACTION_LOCK) {
						transactionsToSend.add(copyTransaction);
					}
				}
				blockSize();
			}
		}
		
	}

	private Transaction copyTransaction(Transaction transaction) {
		Transaction copy = new Transaction();
		copy.to = transaction.to;
		copy.from = transaction.from;
		copy.amount = transaction.amount;
		copy.date = new Date(transaction.date.getTime());

		return copy;
		
	}

	private void blockSize() {
		synchronized (BLOCK_LOCK) {
			if (block.transactions.size() == 8) {
				block.blockHash = getHash(block.transactions);
				try {
					writeBlockFile(OBJECT_MAPPER.writeValueAsString(block));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				block.transactions.clear();
				blocksToSend.add(block);
			}
		}
	}
	

	public String getHash(List<Transaction> transList) {
		StringBuilder sBuilder = new StringBuilder();
		for (Transaction t: transList) {
			sBuilder.append(t.transactionHash);
		}
		return getHash(sBuilder.toString());
	}
	
	public String getHash(String toHash) {
		return DigestUtils.sha256Hex(toHash);
	}

	public void addBlocks(String json) {
		List<Block> blocks;
		try {
			blocks = OBJECT_MAPPER.readValue(json, new TypeReference<List<Block>>(){});
			addBlocks(blocks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addBlocks(List<Block> blocks) {
		synchronized (BLOCK_LOCK) {
			System.out.println("block locking");
			for (int i = 0; i < blocks.size(); i++) {
				int blockId = blocks.get(i).blockId;
				synchronized (CONFIG_LOCK) {
					System.out.println("conf locking");
					//Sent block Id >= our block Id.
					if (blockId >= CONF.unfinishedBlockId ) {
						if (blocks.get(i).transactions.size() == 8) {
							System.out.println("writing file");
							try {
								writeBlockFile(OBJECT_MAPPER.writeValueAsString(blocks.get(0)));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							blocksToSend.add(blocks.get(i));
						} else {
							//Smaller value block
						}
					}
				}
				
			}
		}
	}
	
	public List<Block> getBlocksToSend() {
		synchronized (BLOCKS_LOCK) {
			return blocksToSend;
		}
	}
	
	public List<Transaction> getTransactionsToSend() {
		synchronized (TRANSACTION_LOCK) {
			return transactionsToSend;
		}
	}

	public List<Peer> getPeers() {
		synchronized (PEERS_LOCK) {
			return peers;
		}
	}

	private void writeBlockFile(String json) {
		synchronized (CONFIG_LOCK) {
			try {
				String fileName = "BLOCK_" + CONF.lastBlockId + ".txt";
				FileWriter fWriter = new FileWriter(fileName);
				fWriter.write(json);
				fWriter.close();
				CONF.lastBlockId += 1;
				CONF.unfinishedBlockId +=1;
				fWriter = new FileWriter(CONFIG, false);
				fWriter.write(OBJECT_MAPPER.writeValueAsString(CONF));
				fWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addPeers(List<Peer> addPeers) {
		synchronized (PEERS_LOCK) {
			for (Peer p:addPeers) {
				if (!peers.contains(p)) {
					peers.add(p);
				}
			}
		}
		
	}

	public int getBlockN() {
		synchronized (BLOCK_LOCK) {
			return block.blockId;
		}
	}


	public void clearBlocksToSend() {
		synchronized (BLOCKS_LOCK) {
			blocksToSend.clear();
		}
		
	}
}
