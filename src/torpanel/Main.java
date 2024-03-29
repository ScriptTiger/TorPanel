package torpanel;

// Main standard swing deps
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

// Main file reader deps
import java.io.*;

// Main version check deps
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

// TorConnector deps
import java.net.*;

public class Main extends JPanel {
	private static TorConnector torConnector = new TorConnector();
	private static JLabel status;

	private Main() {

		// JPanel properties
		setPreferredSize(new Dimension(220, 80));
		setLayout(null);
		setBackground(Color.WHITE);

		// Status label
		status = new JLabel("Disconnected", SwingConstants.CENTER);
		add(status);
		status.setBounds(0, 0, 220, 50);
		status.setFont(new Font("SansSerif", Font.PLAIN, 15));
		status.setForeground(Color.RED);

		// New identity button
		JButton newnym = new JButton("New Identity");
		add(newnym);
		newnym.setBounds(5, 50, 105, 25);
		newnym.setFont(new Font("SansSerif", Font.BOLD, 12));
		newnym.addActionListener(e -> torConnector.newnym());

		// Reload button
		JButton reload = new JButton("Reload");
		add(reload);
		reload.setBounds(110, 50, 105, 25);
		reload.setFont(new Font("SansSerif", Font.BOLD, 12));
		reload.addActionListener(e -> torConnector.reload());
	}

	public static void main(String[] args) {
		String version;
		boolean check = true;

		// Construct and set up jFrame
		JFrame jFrame = new JFrame("TorPanel");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.getContentPane().add(new Main());
		jFrame.pack();
		jFrame.setAlwaysOnTop(true);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);

		// Read configuration from torpanel.conf file if it exists
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("torpanel.conf")));

			String configLine;

			while ((configLine = reader.readLine()) != null) {
				String[] tokens = configLine.split("=", 2);
				switch (tokens[0].toLowerCase()) {
					case "host":
						torConnector.setHost(tokens[1]);
						break;
					case "port":
						try {torConnector.setPort(Integer.parseInt(tokens[1]));
						} catch (Exception err) {}
						break;
					case "secret":
						torConnector.setSecret(tokens[1]);
						break;
					case "check":
						if (tokens[1].toLowerCase().equals("false")) {check = false;}
						break;
				}
			}
			reader.close();
		} catch (Exception err) {}

		// Show jFrame
		jFrame.setVisible(true);

		// Set the connection status
		version = torConnector.getVersion();
		if (version != null && !version.equals("")) {
			status.setVisible(false);
			status.setText("<html><center>"+torConnector.getHost()+":"+torConnector.getPort()+"<br/>"+version+"</html>");
			status.setForeground(Color.GREEN);
			status.setVisible(true);

			// Check for updates if enabled
			if (check) {
				try {
					// Get web page
					URL url = new URL("https://www.torproject.org/download/tor/");
					HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
					InputStream responseStream = (InputStream) conn.getContent();

					// Make web page reader
					BufferedReader reader = new BufferedReader(
						new InputStreamReader(responseStream, StandardCharsets.UTF_8)
					);

					// Compare latest version with local version
					String responseLine;
					Pattern pattern = Pattern.compile("^.*\\s\\(tor\\s.*$");
					while ((responseLine = reader.readLine()) != null) {
						Matcher matcher = pattern.matcher(responseLine);
						if (matcher.matches()) {
							String local = version.replaceAll("\\s.*$", "");
							String latest = responseLine
								.replaceAll("^.*r\\s", "")
								.replaceAll("\\)<.*$", "");
							if (!local.equals(latest)) {new popupDialog("Warning", "Your current Tor version is "+local+", but binaries are available for the latest stable version of "+latest+".\n\n"+url.toString(), JOptionPane.WARNING_MESSAGE);}
							break;
						}
					}
				} catch (Exception err) {}
			}
		}
	}
}

// Generic popup dialog
class popupDialog {
	public popupDialog(String title, String msg, int type) {
		JOptionPane optionPane = new JOptionPane(msg, type);
		JDialog dialog = optionPane.createDialog(title);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}
}

class TorConnector {

	private String host = "127.0.0.1";
	private int port = 9051;
	private String secret = "";

	// Setters
	public void setHost(String host) {this.host = host;}
	public void setPort(int port) {this.port = port;}
	public void setSecret(String secret) {this.secret = secret;}

	// Getters
	public String getHost() {return host;}
	public int getPort() {return port;}

	// Class to handle socket lifetime, and associated reader and writer
	private class SocketInfo {

		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;

		// Constructor
		public SocketInfo(Socket socket, BufferedReader reader, PrintWriter writer) {
			this.socket = socket;
			this.reader = reader;
			this.writer = writer;
		}

		// Method to handle reader
		public String read() {
			String msg;
			try {
				String tryMsg = reader.readLine();
				msg = tryMsg;
		 	} catch (Exception err) {return null;}
			return msg;
		}

		// Method to handle writer
		public void write(String msg) {writer.println(msg);}

		// Method to close socket
		public void close() {
			try {socket.close();
			} catch (Exception err) {}
		}
	}

	private SocketInfo torConnect() {

		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter writer = null;

		try {
			Socket trySocket = new Socket(this.host, this.port);
			BufferedReader tryReader = new BufferedReader(new InputStreamReader(trySocket.getInputStream()));
			PrintWriter tryWriter = new PrintWriter(trySocket.getOutputStream(), true);
			tryWriter.println("authenticate \""+secret+"\"");
			if (!tryReader.readLine().equals("250 OK")) {
				trySocket.close();
				new popupDialog("Error", "Failed to authenticate", JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				socket = trySocket;
				reader = tryReader;
				writer = tryWriter;
			}
		
		} catch (Exception err) {
			new popupDialog("Error", err.getMessage(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return new SocketInfo(socket, reader, writer);
	}

	private String torSend(String txMsg) {

		SocketInfo socketInfo = torConnect();

		if (socketInfo == null) {return null;}

		socketInfo.write(txMsg);

		String rxMsg = socketInfo.read();

		socketInfo.write("quit");

		socketInfo.close();

		return rxMsg;
	}

	public String getVersion() {
		String version = torSend("getinfo version");
		if (version != null) {version = version.replace("250-version=", "");}
		return version;
	}

	// Signals
	public void newnym() {torSend("signal newnym");}
	public void reload() {torSend("signal reload");}
}
