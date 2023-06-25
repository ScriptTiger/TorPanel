package torpanel

// Main standard swing deps
import java.awt.Dimension
import javax.swing.JLabel
import java.awt.Font
import java.awt.Color
import javax.swing.SwingConstants
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

// Main file reader deps
import java.io.BufferedReader
import java.io.FileReader
import java.io.File

// ErrorDialog deps
import javax.swing.JOptionPane

// TorConnector deps
import java.net.Socket
import java.io.InputStreamReader
import java.io.PrintWriter

fun main() {

	// Construct torConnector
	val torConnector = TorConnector()

	// Set up jFrame
	val jFrame = JFrame("TorPanel")
	jFrame.getContentPane().setPreferredSize(Dimension(220, 80))
	jFrame.getContentPane().setLayout(null)
	jFrame.getContentPane().setBackground(Color.WHITE)
	jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	jFrame.pack()
	jFrame.setAlwaysOnTop(true)
	jFrame.setResizable(false)
	jFrame.setLocationRelativeTo(null)

	// Status label
	val status = JLabel("Disconnected", SwingConstants.CENTER)
	jFrame.getContentPane().add(status)
	status.setBounds(0, 0, 220, 50)
	status.setFont(Font("SansSerif", Font.PLAIN, 15))
	status.setForeground(Color.RED)

	// New identity button
	val newnym = JButton("New Identity")
	jFrame.getContentPane().add(newnym)
	newnym.setBounds(5, 50, 105, 25)
	newnym.setFont(Font("SansSerif", Font.BOLD, 12))
	newnym.addActionListener() {torConnector.newnym()}

	// Reload button
	val reload = JButton("Reload")
	jFrame.getContentPane().add(reload)
	reload.setBounds(110, 50, 105, 25)
	reload.setFont(Font("SansSerif", Font.BOLD, 12))
	reload.addActionListener() {torConnector.reload()}

	// Read configuration from torpanel.conf file if it exists
	try {
		BufferedReader(FileReader(File("torpanel.conf"))).use {reader ->
			reader.lines().forEach {
				val tokens = it.split("=", limit = 2)
				when(tokens[0].lowercase()) {
					"host"	-> torConnector.host = tokens[1]
					"port" -> torConnector.port = Integer.parseInt(tokens[1])
					"secret" -> torConnector.setSecret(tokens[1])
				}
			}
		}
	} catch (err: Exception) {}

	// Show jFrame
	jFrame.setVisible(true)

	// Set the connection status
	val version = torConnector.getVersion()
	if (version != null && !version.equals("")) {
		status.setText("<html><center>"+torConnector.host+":"+torConnector.port+"<br/>"+version+"</html>")
		status.setForeground(Color.GREEN)
	}
}

private class ErrorDialog(val msg: String?) {
	init {
		val dialog = JOptionPane(msg, JOptionPane.ERROR_MESSAGE).createDialog("Error")
		dialog.setAlwaysOnTop(true)
		dialog.setVisible(true)
	}
}

private class TorConnector {
	var host: String = "127.0.0.1"
	var port: Int = 9051
	private var secret: String = ""

	fun setSecret(secret: String) {this.secret = secret}

	private class SocketInfo(val socket: Socket, val reader: BufferedReader, val writer: PrintWriter) {
		fun read(): String {return reader.readLine()}
		fun write(msg: String) {writer.println(msg)}
		fun close() {socket.close()}
	}

	private fun torConnect(): SocketInfo? {
		var socketInfo: SocketInfo

		try {
			val trySocket = Socket(host, port)
			val tryReader = BufferedReader(InputStreamReader(trySocket.getInputStream()))
			val tryWriter = PrintWriter(trySocket.getOutputStream(), true)
			tryWriter.println("authenticate \""+secret+"\"")
			if (!tryReader.readLine().equals("250 OK")) {
				trySocket.close()
				ErrorDialog("Failed to authenticate")
				return null
			} else {socketInfo = SocketInfo(trySocket, tryReader, tryWriter)}
		
		} catch (err: Exception) {
			ErrorDialog(err.message)
			return null
		}

		return socketInfo
	}

	private fun torSend(txMsg: String): String? {
		val socketInfo = torConnect()
		if (socketInfo == null) {return null}
		socketInfo.write(txMsg)
		val rxMsg = socketInfo.read()
		socketInfo.write("quit")
		socketInfo.close()
		return rxMsg
	}

	fun getVersion(): String? {
		var version = torSend("getinfo version")
		if (version != null) {version = version.replace("250-version=", "")}
		return version
	}

	// Signals
	fun newnym() {torSend("signal newnym")}
	fun reload() {torSend("signal reload")}
}