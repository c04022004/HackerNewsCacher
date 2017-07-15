import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * The UI part of the HackerNewsDownloader
 */
public class HackerNewsUI {
	final private String productName = "HN Cacher v1.0";
	final private int maxNumberOfThreads;
	private String savePath;
	private int saveInterval = 60;
	private int saveItems = 30;
	private ScheduledExecutorService downloadScheduler = Executors.newScheduledThreadPool(1);

	private JFrame frame = new JFrame();
	private JLabel label1 = new JLabel(productName);

	private JLabel chooseLabel;
	private JTextField chooseText;
	private JButton chooseButton;

	private JLabel autoLabel;
	private JComboBox autoBox;
	private JButton manualButton;

	private JLabel itemsLabel;
	private JComboBox itemsBox;
	private JCheckBox commentBox;

	private JTextArea dispBox;
	private JScrollPane scrollPane;

	/**
	 * HackerNewsUI constructor.
	 * 
	 * @param maxNumberOfThreads
	 *            number of threads to be used in multithreading
	 */
	public HackerNewsUI(int maxNumberOfThreads) {
		this.maxNumberOfThreads = maxNumberOfThreads;
	}

	/**
	 * Put all the components in the window.
	 */
	public void createAndShowGUI() {
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container pane = frame.getContentPane();

		dispBox = new JTextArea("", 15, 40);
		dispBox.setEditable(false);
		scrollPane = new JScrollPane(dispBox);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		chooseLabel = new JLabel("Save Location: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(chooseLabel, c);

		chooseText = new JTextField(20);
		chooseText.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.EAST;
		buttonPane.add(chooseText, c);

		chooseButton = new JButton("Choose...");
		chooseButton.addActionListener(new LoadListener());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		buttonPane.add(chooseButton, c);

		autoLabel = new JLabel("Cache Interval: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(autoLabel, c);

		String[] intervalOptions = { "1 hour", "3 hours", "6 hours", "12 hours", "24 hours" };
		autoBox = new JComboBox(intervalOptions);
		autoBox.setSelectedIndex(0);
		autoBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = (String) ((JComboBox) e.getSource()).getSelectedItem();
				if (str.equals("1 hour")) {
					saveInterval = 1 * 60;
				} else if (str.equals("3 hours")) {
					saveInterval = 3 * 60;
				} else if (str.equals("6 hours")) {
					saveInterval = 6 * 60;
				} else if (str.equals("12 hours")) {
					saveInterval = 12 * 60;
				} else if (str.equals("24 hours")) {
					saveInterval = 24 * 60;
					saveInterval = 1;
					// System.out.println("saveInterval changed to 1");
				}
				downloadScheduler.shutdownNow();
				downloadScheduler = Executors.newScheduledThreadPool(1);
				downloadScheduler.scheduleAtFixedRate(new DownloadAndUpdate(), saveInterval,
						saveInterval, TimeUnit.MINUTES);
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(autoBox, c);

		manualButton = new JButton("Do it manually");
		manualButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingWorker task = new DownloadAndUpdate();
				task.execute();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(manualButton, c);

		itemsLabel = new JLabel("# of items: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(itemsLabel, c);

		String[] itemsOptions = { "30", "50", "100", "200", "500" };
		itemsBox = new JComboBox(itemsOptions);
		itemsBox.setSelectedIndex(0);
		itemsBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = (String) ((JComboBox) e.getSource()).getSelectedItem();
				switch (str) {
					case "30":
						saveItems = 30;
						break;
					case "50":
						saveItems = 50;
						break;
					case "100":
						saveItems = 100;
						break;
					case "200":
						saveItems = 200;
						break;
					case "500":
						saveItems = 500;
						break;
				}
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(itemsBox, c);

		commentBox = new JCheckBox("include comment");
		commentBox.setSelected(true);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		buttonPane.add(commentBox, c);

		// pane.setLayout(new BorderLayout());

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BorderLayout());
		labelPane.add(label1, BorderLayout.NORTH);

		labelPane.add(new JSeparator(), BorderLayout.CENTER);
		labelPane.add(buttonPane, BorderLayout.SOUTH);
		labelPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pane.add(labelPane, BorderLayout.NORTH);
		// pane.add(buttonPane, BorderLayout.CENTER);
		// pane.add(scrollPane, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
		frame.setTitle(productName);

	}

	/**
	 * Invoke file chooser and display the path.
	 * 
	 * @return <code>true</code> if the successful;
	 *         <p>
	 *         <code>false</code> otherwise.
	 */
	private Boolean getFilePath() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new java.io.File(".."));
		fc.setDialogTitle("Choose a directory to store all PDFs");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			savePath = file.getAbsolutePath();
			chooseText.setText(savePath);
			System.out.println(savePath);
			return true;
		} else {
			System.out.println("File selection cancelled");
			return false;
		}
	}

	/**
	 * Trigger the file chooser until a directory is selected.
	 */
	private void setupEnvironment() {
		while (!getFilePath()) {
		}
		downloadScheduler.scheduleAtFixedRate(new DownloadAndUpdate(), saveInterval, saveInterval,
				TimeUnit.MINUTES);
	}

	/**
	 * LoadListener Class to listen to the choose button.
	 */
	class LoadListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 * <p>
		 * Trigger the file chooser, refresh the GUI to show the path.
		 */
		@Override
		public void actionPerformed(ActionEvent event) {
			getFilePath();
		}
	}

	/**
	 * Trigger the downloader and update the GUI correspondingly.
	 */
	class DownloadAndUpdate extends SwingWorker<Object, Integer> {

		@Override
		protected Object doInBackground() throws Exception {
			try {
				AtomicInteger counter = new AtomicInteger(saveItems);
				Collection<Integer> updatedList = HackerNewsListener.getInstance()
						.getTopN(saveItems);
				HackerNewsDownloader downloader = new HackerNewsDownloader(updatedList, savePath,
						commentBox.isSelected(), maxNumberOfThreads, counter);
				Thread downloaderThread = new Thread(downloader);
				downloaderThread.start();
				while (!isCancelled() && downloaderThread.isAlive()) {
					Thread.sleep(500);
					publish(new Integer(counter.get()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void process(List<Integer> pairs) {
			int remainingItems = pairs.get(pairs.size() - 1);
			frame.setTitle(productName + " - ("
					+ String.format("%.0f", (1 - (1.0 * remainingItems / saveItems)) * 100)
					+ "% done)");
		}

	}

	/**
	 * Entry point of the program
	 * 
	 * @param args
	 *            an integer indicating the number of threads used
	 */
	public static void main(String args[]) {
		HackerNewsUI myGUI;
		if (args.length > 0) {
			int numThreads = Integer.parseInt(args[0]);
			myGUI = new HackerNewsUI(numThreads);
		} else {
			myGUI = new HackerNewsUI(2);
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				myGUI.createAndShowGUI();
				myGUI.setupEnvironment();
			}
		});
	}
}
