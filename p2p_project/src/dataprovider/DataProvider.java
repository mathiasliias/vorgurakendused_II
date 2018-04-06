package dataprovider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.codec.digest.DigestUtils;

import data.Transaction;
import httpclient.Requests;

public class DataProvider {
	
	private static final Requests requests = new Requests();
	private final static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EET"));

	private static List<String> ipAddressList = Arrays.asList("127.0.0.1:4040"/*, "127.0.0.1:8080"*/);
	private static List<String> users = Arrays.asList("Tanel", "Tammet");
	
	
	public static void main(String[] args) {
		provideTransactions();
	}
	
	public static void provideTransactions() {
		for (int i= 0; i < 50; i++) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Transaction t = getRandomTransaction();
			for (String ipAddress: ipAddressList) {
				String[] ip = ipAddress.split(":");
				requests.postTransactionRequest(ip[0], Integer.parseInt(ip[1]), t);
			}	
		}
	}

	private static Transaction getRandomTransaction() {
		Transaction transaction = new Transaction();
		transaction.amount = BigDecimal.valueOf(Math.random() * 100).setScale(2, RoundingMode.HALF_UP);
		transaction.date = calendar.getTime();
		int randomInt = (int) Math.round(Math.random());
		transaction.to = users.get(randomInt);
		int other = randomInt == 0 ? 1 : 0;
		transaction.from = users.get(other);
		transaction.transactionHash = DigestUtils.sha256Hex(transaction.to + transaction.from + transaction.date + transaction.amount);
		return transaction;
	}
}
