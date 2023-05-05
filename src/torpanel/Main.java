package torpanel;

// Main standard javafx fxml deps
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main file reader deps
import java.io.*;

// Main additional type deps
import javafx.scene.paint.Color;

// Alert deps
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

// TorConnector deps
import java.net.*;
import java.io.*;

public class Main extends Application {

	private TorConnector torConnector;
	private Controller controller;

	@Override
	public void start(Stage mainStage) {

		Parent parent = null;

		// Load fxml
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/torpanel/gui/main.fxml"));
		try {
			Parent tryParent = loader.load();
			parent = tryParent;
		} catch (Exception err) {new ErrorDialog(err.getMessage());}

		// Construct torConnector
		torConnector = new TorConnector(mainStage);

		// Get controller and share torConnector with it
		controller = loader.getController();
		controller.setTorConnector(torConnector);

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
				}
			}
			reader.close();
		} catch (Exception err) {}

		// Finish setting the mainStage and show it
		mainStage.setTitle("TorPanel");
		mainStage.setAlwaysOnTop(true);
		mainStage.setResizable(false);
		mainStage.setScene(new Scene(parent));
		mainStage.show();

		// Set the connection status
		setStatus();
	}

	public static void main(String[] args) {launch(args);}

	private void setStatus() {
		String version = torConnector.getVersion();
		if (version != null && !version.equals("")) {
			controller.setStatusText(torConnector.getHost()+":"+torConnector.getPort()+"\n"+version);
			controller.setStatusFill(Color.GREEN);
		}
	}
}

class ErrorDialog {

	// Constructor for a child alert
	public ErrorDialog(String msg, Stage stage) {
		Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
		alert.initOwner(stage);
		alert.show();
	}

	// Constructor for a stand-alone alert
	public ErrorDialog(String msg) {
		Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
		alert.show();
	}
}

class TorConnector {

	private Stage mainStage;
	private String host = "127.0.0.1";
	private int port = 9051;
	private String secret = "";

	// Constructor
	public TorConnector(Stage mainStage) {this.mainStage = mainStage;}

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
				new ErrorDialog("Failed to authenticate", mainStage);
				return null;
			} else {
				socket = trySocket;
				reader = tryReader;
				writer = tryWriter;
			}
		
		} catch (Exception err) {
			new ErrorDialog(err.getMessage(), mainStage);
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