import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Get the updated top500 list from the web.
 */
public class HackerNewsListener {
	final public static String itemUrl = "https://hacker-news.firebaseio.com/v0/item/";
	final public static String top500Url = "https://hacker-news.firebaseio.com/v0/topstories";
	final public static String jsonSuffix = ".json";
	final public static String commentUrl = "https://news.ycombinator.com/item?id=";
	final private static HackerNewsListener instance = new HackerNewsListener();

	private HackerNewsListener() {

	}

	/**
	 * Implement singleton.
	 * 
	 * @return a unique instance of the HackerNewsListener
	 */
	public static HackerNewsListener getInstance() {
		return instance;
	}

	/**
	 * Get the top N items in the HN top500 list.
	 * 
	 * @param n
	 *            Number of items to be downloaded, an int less than or equal
	 *            500
	 * @return A list of HN item ID
	 * @throws IOException
	 */
	public Collection<Integer> getTopN(int n) throws IOException {

		ArrayList<Integer> top500Items = new ArrayList<Integer>();

		URL url = new URL(top500Url + jsonSuffix);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		// int responseCode = conn.getResponseCode();
		// System.out.println("\nSending 'GET' request to URL : " + url);
		// System.out.println("Response Code : " + responseCode);

		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

			String responseStr = in.readLine().toString();
			String[] tokens = responseStr.substring(1, responseStr.length() - 1).split(",");
			for (String tk : tokens) {
				top500Items.add(Integer.parseInt(tk));
			}
			// System.out.println(top500Items);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e);
		}

		return top500Items.subList(0, n);
	}

}
