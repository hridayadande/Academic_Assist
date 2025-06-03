module TP3 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.engine;
	requires org.junit.platform.commons;
	requires org.junit.platform.engine;
	
	opens application to javafx.graphics, javafx.fxml;
	opens Jtesting to org.junit.platform.commons;
	exports application;
	exports databasePart1;
	exports Jtesting;
}