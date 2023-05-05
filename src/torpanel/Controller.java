package torpanel;

// Controller standard fxml deps
import javafx.fxml.FXML;

// Label deps
import javafx.scene.control.Label;

// Additional type deps
import javafx.scene.paint.Color;

public class Controller {

	private TorConnector torConnector;

	@FXML
	private Label status;

	// Shared torConnector from Main
	public void setTorConnector(TorConnector torConnector) {this.torConnector = torConnector;}

	// Button OnAction methods
	public void newnym() {torConnector.newnym();}
	public void reload() {torConnector.reload();}

	// Status label methods
	public void setStatusText(String text) {status.setText(text);}
	public void setStatusFill(Color fill) {status.setTextFill(fill);}
}