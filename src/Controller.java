import com.sun.javafx.application.PlatformImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public Button button;
    public ProgressBar progressBar;

    public int amountOfPages = 0;
    public int amountOfDumps = 0;
    public int amountPerMin = 0;

    public javafx.scene.control.TextField inputPages;
    public javafx.scene.control.TextField inputDumps;
    public javafx.scene.control.TextField inputMaxAmountPerMin;
    public Label finishedLabel;

    public SimpleStringProperty simpleStringProperty = new SimpleStringProperty();
    public SimpleStringProperty simpleStringPropertyDumps = new SimpleStringProperty();
    public SimpleStringProperty simpleStringPropertyLabel = new SimpleStringProperty();
    public SimpleStringProperty simpleStringPropertyPerMin = new SimpleStringProperty();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputPages.textProperty().bindBidirectional(simpleStringProperty);
        inputDumps.textProperty().bindBidirectional(simpleStringPropertyDumps);
        inputMaxAmountPerMin.textProperty().bindBidirectional(simpleStringPropertyPerMin);
        finishedLabel.textProperty().bindBidirectional(simpleStringPropertyLabel);

        button.setOnAction(event -> {
            new Thread(() -> {
                try {
                    try {
                        amountOfPages = Integer.parseInt(simpleStringProperty.get());
                        PlatformImpl.runLater(() -> simpleStringPropertyLabel.setValue("Scraping " + amountOfPages + " pages..."));

                    } catch (NumberFormatException e) {
                        try {
                            amountOfDumps = Integer.parseInt(simpleStringPropertyDumps.get());
                            PlatformImpl.runLater(() -> simpleStringPropertyLabel.setValue("Scraping " + amountOfDumps + " dumps..."));
                        } catch (NumberFormatException n) {
                            System.out.println("User did not enter a valid number");
                        }
                    }

                    long begin = System.currentTimeMillis();

                    Scraper scraper = new Scraper(amountOfPages, amountOfDumps);
                    ArrayList<String> links = scraper.linkScraper();
                    try {
                        amountPerMin = Integer.parseInt(simpleStringPropertyPerMin.get());
                    } catch (NumberFormatException e) {
                        System.out.println("Amount per minute empty");
                    }

                    long duration = 0L;
                    int counter = 0;
                    for (String link : links) {

                        long start = System.currentTimeMillis();

                        System.out.println(link);
                        scraper.pageDetails(link);
                        scraper.commentsOfPage(link);

                        duration += (System.currentTimeMillis() - start);

                        if (++counter == amountPerMin && amountPerMin != 0) {
                            if (duration < 60_000) {
                                long difference = 60_000 - duration;
                                System.out.println("Was too quick in scraping, sleeping for " + difference + " ms.");
                                try {
                                    Thread.sleep(difference);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            duration = 0;
                            counter = 0;
                        }
                    }
                    PlatformImpl.runLater(() -> simpleStringPropertyLabel.setValue("FINISHED!"));
                    System.out.println(((System.currentTimeMillis() - begin)/1000.0) + " seconds");
                    System.out.println("-FINISHED-");


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        });

        progressBar.progressProperty().bind(Scraper.doubleProperty);
    }
}
