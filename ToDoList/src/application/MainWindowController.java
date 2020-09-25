package application;


import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.Node;

public class MainWindowController {
	private List<TodoItem> todoItems;
	@FXML
	private ListView<TodoItem> todoListView;
	@FXML
	private TextArea itemDetailsTextArea;
	@FXML
	private Label deadlineLabel;
	@FXML
	private BorderPane mainBorderPane;
	@FXML
	private ContextMenu listContextMenu;
	@FXML
	private ToggleButton filterToggleButton;
	
	private FilteredList<TodoItem> filteredList;
	private Predicate<TodoItem> wantAllItems;
	private Predicate<TodoItem>wantTodaysItems;

	public void initialize() {
		listContextMenu = new ContextMenu();
		MenuItem deleteMenuItem= new MenuItem("Delete");
		deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				TodoItem item = todoListView.getSelectionModel().getSelectedItem();
				deleteItem(item);
			}
		});
		listContextMenu.getItems().addAll(deleteMenuItem);
		todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {

			@Override
			public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
				// TODO Auto-generated method stub
				if (newValue != null) {
					TodoItem item = todoListView.getSelectionModel().getSelectedItem();
					itemDetailsTextArea.setText(item.getDetails());
					DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy");
					deadlineLabel.setText(df.format(item.getDeadline()));
				}

			}

		});
		
		wantAllItems = new Predicate<TodoItem>() {

			@Override
			public boolean test(TodoItem t) {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		wantTodaysItems = new Predicate<TodoItem>() {

			@Override
			public boolean test(TodoItem todoItem) {
				// TODO Auto-generated method stub
				return (todoItem.getDeadline().equals(LocalDate.now()));
			}
			
		};
		filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
		SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList,new Comparator<TodoItem>() {
			
			@Override
			public int compare(TodoItem o1,TodoItem o2) {
				return o1.getDeadline().compareTo(o2.getDeadline());
			}
		});

		//todoListView.setItems(TodoData.getInstance().getTodoItems());
		todoListView.setItems(sortedList);
		todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		todoListView.getSelectionModel().selectFirst();
		todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
			
			@Override
			public ListCell<TodoItem> call(ListView<TodoItem> param) {
				// TODO Auto-generated method stub
				ListCell<TodoItem> cell = new ListCell<TodoItem>() {
					@Override
					protected void updateItem(TodoItem item,boolean empty) {
						super.updateItem(item, empty);
						if(empty) {
							setText(null);
						}else {
							setText(item.getShortDescription());
							if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
								setTextFill(Color.RED);
							}else if(item.getDeadline().equals(LocalDate.now().plusDays(1))) {
								setTextFill(Color.BROWN);
							}
						}
					}
				};
					
					cell.emptyProperty().addListener(
							(obs,wasEmpty,isNowEmpty) ->{
								if(isNowEmpty) {
									cell.setContextMenu(null);
								}else {
									cell.setContextMenu(listContextMenu);
								}
							}
							
							);
				
					return cell;
				
			}
		});

	}

	@FXML
	public void showNewItemDialog() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(mainBorderPane.getScene().getWindow());
		dialog.setTitle("Add new todoItem");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("TodoItemDialog.fxml"));
		try {

			dialog.getDialogPane().setContent(fxmlLoader.load());

		} catch (IOException e) {
			System.out.println("Couldn't load the dialog");
			e.printStackTrace();
			return;
		}
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			TodoItemDialogController controller = fxmlLoader.getController();
			TodoItem newItem = controller.processResults();
			//todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
			todoListView.getSelectionModel().select(newItem);
			System.out.println("OK pressed");
		} else {
			System.out.println("Cancel pressed");
		}
	}
	@FXML
	public void handleKeyPressed(KeyEvent keyEvent) {
		TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
	}

	@FXML
	public void handleClickListView() {
		TodoItem item = todoListView.getSelectionModel().getSelectedItem();
		itemDetailsTextArea.setText(item.getDetails().toString());
	}
	
	public void deleteItem(TodoItem item) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Todo Item");
		alert.setHeaderText("Delete item: "+item.getShortDescription());
		alert.setContentText("Are you sure? Press OK to confirm, or cancel to Back out.");
		Optional<ButtonType> result = alert.showAndWait();
		
		if(result.isPresent()&&result.get()==ButtonType.OK) {
			TodoData.getInstance().deleteTodoItem(item);
		}
	}
	@FXML
	public void handleFilter() {
		TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
		if(filterToggleButton.isSelected()) {
			filteredList.setPredicate(wantTodaysItems);
			if(filteredList.isEmpty()) {
				itemDetailsTextArea.clear();
				deadlineLabel.setText("");
			}else if(filteredList.contains(selectedItem)) {
				todoListView.getSelectionModel().select(selectedItem);
			}else {
				todoListView.getSelectionModel().selectFirst();
			}
			
		}else {
			filteredList.setPredicate(wantAllItems);
			todoListView.getSelectionModel().select(selectedItem);
		}
	}
	@FXML
	public void handleExit() {
		Platform.exit();
	}

}
