package application;
	
import java.io.IOException;

import datamodel.TodoData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;



public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
			Scene scene = new Scene(root,900,500);
			primaryStage.setTitle("Todo List");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		try {
			TodoData.getInstance().storeTodoItems();
			
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		try {
			TodoData.getInstance().loadTodoItems();
			
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
}
