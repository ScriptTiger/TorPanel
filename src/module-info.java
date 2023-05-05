module torpanel {
	requires javafx.fxml;
	requires javafx.controls;

	opens torpanel to javafx.fxml;
	exports torpanel;
}
