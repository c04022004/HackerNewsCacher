import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

/**
 * The actual HackerNewsDownloader core
 */
public class HackerNewsDownloader implements Runnable {

	final private String web2pdfPath = "/usr/local/bin/wkhtmltopdf";
	private Collection<Integer> downloadList;
	private String savePath;
	private String saveDir;
	private boolean downloadComments = false;
	private ExecutorService downloadPool;
	private AtomicInteger counter = null;

	/**
	 * HackerNewsDownloader constructor.
	 * 
	 * @param downloadList
	 *            A list of HN item ID
	 * @param savePath
	 *            Absolute path to the download directory
	 * @param downloadComments
	 *            also download comments if set to <code>true<\code>
	 * @param maxConnections
	 *            number of threads used in multithreading
	 * @param counter
	 *            reference to a progress counter
	 */
	public HackerNewsDownloader(Collection<Integer> downloadList, String savePath,
			boolean downloadComments, int maxConnections, AtomicInteger counter) {
		this.downloadList = downloadList;
		this.savePath = savePath;
		this.downloadComments = downloadComments;
		this.downloadPool = Executors.newFixedThreadPool(maxConnections);
		this.counter = counter;
	}

	@Override
	public void run() {
		Long longTime = new Long(new Date().getTime() / 1000);
		saveDir = savePath + "/" + longTime + "/";
		try {
			Path dir = Paths.get(saveDir);
			Files.createDirectory(dir);
			int i = 1;
			for (Integer item : downloadList) {
				downloadPool.submit(new DownloadThread(item, i++));
			}
			downloadPool.shutdown();
			downloadPool.awaitTermination(600, TimeUnit.SECONDS);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// public void startDownload() throws IOException, InterruptedException {
	//
	// Path dir = Paths.get(savePath);
	// Files.createDirectory(dir);
	// int i = 1;
	// for (Integer item : downloadList) {
	// downloadPool.submit(new DownloadThread(item, i++));
	// }
	// downloadPool.shutdown();
	// downloadPool.awaitTermination(600, TimeUnit.SECONDS);
	// }

	/**
	 * Download thread for multi-threading.
	 */
	class DownloadThread implements Runnable {

		final int itemNo;
		final int rankNum;

		/**
		 * Create a thread per HN item
		 * 
		 * @param itemNo
		 *            item ID to be downloaded
		 * @param rankNum
		 *            current rank
		 */
		DownloadThread(int itemNo, int rankNum) {
			this.itemNo = itemNo;
			this.rankNum = rankNum;
		}

		@Override
		/**
		 * {@inheritDoc}
		 * <p>
		 */
		public void run() {
			try {
				JSONObject itemEntry = getJsonFromWeb(
						HackerNewsListener.itemUrl + itemNo + HackerNewsListener.jsonSuffix);
				if (itemEntry.get("type").equals("story")) {
					String itemUrl = (String) itemEntry.get("url");
					String itemTitle = (String) itemEntry.get("title");
					if (itemTitle.contains("[pdf]")) {
						URL url = new URL(itemUrl);
						InputStream in = url.openStream();
						Files.copy(in,
								Paths.get(saveDir + String.format("%03d", rankNum) + "_"
										+ itemTitle.substring(0, itemTitle.length() - 6) + ".pdf"),
								StandardCopyOption.REPLACE_EXISTING);
						in.close();
					} else {
						Process process = (new ProcessBuilder(web2pdfPath, "--image-quality", "80",
								itemUrl, saveDir + String.format("%03d", rankNum) + "_" + itemTitle
										+ ".pdf")).start();
						process.waitFor();
						// if (process.exitValue() > 0) {
						// process.getErrorStream();
						// // throw new IOException("wkhtmltopdf ended
						// // unexpectedly");
						// }
					}
					if (downloadComments) {
						Process process = (new ProcessBuilder(web2pdfPath, "--image-quality", "50",
								HackerNewsListener.commentUrl + itemNo,
								saveDir + String.format("%03d", rankNum) + "_" + itemTitle
										+ "_comments.pdf")).start();
						process.waitFor();
					}
					System.out.println("Rank" + rankNum + "-" + itemNo + " finished");
				} else {
					System.out.println(
							"Rank" + rankNum + "-" + itemNo + " type=" + itemEntry.get("type"));
				}
			} catch (IOException | InterruptedException e) {
				System.out.println("Rank" + rankNum + "-" + itemNo + " incomplete");
				e.printStackTrace();
			} finally {
				if (counter != null) {
					counter.getAndDecrement();
				}
			}
		}
	}

	/**
	 * Get the Json description of the HN item.
	 * 
	 * @param link
	 *            link to the HN site
	 * @return Description of the HN item
	 * @throws IOException
	 *             when internet connection fails.
	 */
	private JSONObject getJsonFromWeb(String link) throws IOException {
		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		// int responseCode = conn.getResponseCode();
		// System.out.println("\nSending 'GET' request to URL : " + url);
		// System.out.println("Response Code : " + responseCode);

		JSONObject responseJson = null;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

			String responseStr = in.readLine().toString();
			responseJson = new JSONObject(responseStr);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return responseJson;
	}

}
