package lk.ijse.dep.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DEPAlert extends Alert {

    public DEPAlert(AlertType alertType, String title, String header, String message, ButtonType... buttonTypes) {
        super(alertType, message, buttonTypes);
        setTitle(title);
        setHeaderText(header);

        String image = null;
        switch (alertType){
            case ERROR:
                image = "/asset/error.png";
                break;
            case INFORMATION:
                image = "/asset/info.png";
                break;
            case WARNING:
                image = "/asset/warning.png";
                break;
        }

        if (image !=null){
            ImageView imgView = new ImageView(new Image(image));
            imgView.setFitWidth(32);
            imgView.setFitHeight(32);
            setGraphic(imgView);
        }
    }
}
