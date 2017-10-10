package Bowerbird;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Bowerbird extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = new Controller();

        primaryStage.setTitle("Bowerbird Media Player");
        primaryStage.setScene(new Scene(controller));

        Image image = new Image(Bowerbird.class.getResourceAsStream("resources/images/Icon.png"));
        primaryStage.getIcons().add(image);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
