package com.dakshin.mediaplayer;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class FXMLController implements Initializable {
    
    MediaPlayer player;
    Media media;
    FileChooser fileChooser;
    Slider time = new Slider();
    Slider vol = new Slider();
    int volume=100;
    boolean mute=false;
    VoiceThread vt;
    double xOffset = 0;
    double yOffset = 0;
    TargetDataLine line;
    String[] arr;
    int songNo=0;
    
    @FXML
    HBox hbox1,slider,functions;
    @FXML
    Label volumePercent,timer1,timer3,voiceStatus;
    Button close;
    int pre_volume;
    
    @FXML
    protected void closeWind(ActionEvent event){
        line.stop();
        line.close();
        Platform.exit();
    }

    @FXML
    protected void minimize(ActionEvent event){
        ((Stage)((Button)event.getSource()).getScene().getWindow()).setIconified(true);
    }
    
    @FXML
    protected void previous(ActionEvent event){
        previous();
    }
    
    @FXML
    protected void play(ActionEvent event){
        pauseAndPlay();
    }
    
    @FXML
    protected void next(ActionEvent event){
        next();
    }
    
    @FXML
    protected void pepper(ActionEvent event){
    }
    
    @FXML
    protected void mute(ActionEvent event){
        muteUnmute();
    }
    
    @FXML
    protected void stop(ActionEvent event){
        stopPlayer();
    }
    
    @FXML
    protected void playList(ActionEvent event){
        
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PlayList.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            final Stage stage = new Stage();            
            
            stage.initStyle(StageStyle.TRANSPARENT);        
            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
        
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add("background.css");
        
            stage.setScene(scene);
            stage.getScene().setFill(Color.TRANSPARENT);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class VoiceThread extends Task{
        
        volatile boolean exit=false;

        @Override
        protected Object call() throws Exception {
            try {
                 
            IamOptions option = new IamOptions.Builder().apiKey("33tnGjHjEHAbwdIfiy31MZYY__2w-evC2quLSKNJZlbY").build();
            
            SpeechToText service = new SpeechToText(option);
            
            service.setEndPoint("https://gateway-lon.watsonplatform.net/speech-to-text/api");
            
            // Signed PCM AudioFormat with 16kHz, 16 bit sample size, mono
            int sampleRate = 16000;
            final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            
            line.start();

            AudioInputStream audio = new AudioInputStream(line);

            RecognizeOptions options = new RecognizeOptions.Builder().interimResults(true).audio(audio).contentType(HttpMediaType.AUDIO_RAW + "; rate=" + sampleRate).addKeyword("hello").keywordsThreshold(0.1f).build();

            service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
                int prev=0;     

                @Override
                public void onTranscription(SpeechRecognitionResults speechResults) {
                    List<SpeechRecognitionResult> list=speechResults.getResults();
                    for(SpeechRecognitionResult s:list){
                        List<SpeechRecognitionAlternative> map=s.getAlternatives();
                        if(map.get(0).getTranscript().trim().equalsIgnoreCase("hello")&&prev!=1){
                            prev=1;
                            System.out.println("Detected");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    listen();
                                }
                            });
                            line.stop();
                            System.out.print(line.getLineInfo());
                            try {
                                Configuration configuration = new Configuration();
                                
                                configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
                                configuration.setDictionaryPath("5001.dic");
                                configuration.setLanguageModelPath("5001.lm");
                                
                                Logger cmRootLogger = Logger.getLogger("default.config");
                                cmRootLogger.setLevel(java.util.logging.Level.OFF);
                                String conFile = System.getProperty("java.util.logging.config.file");
                                if (conFile == null) {
                                    System.setProperty("java.util.logging.config.file", "ignoreAllSphinx4LoggingOutput");
                                }
                                
                                LiveSpeechRecognizer recognize = new LiveSpeechRecognizer(configuration);
                                recognize.startRecognition(true);
                                SpeechResult result;
                                
                                
                                
                                System.out.println("Initialized");
                                
                                
                                while ((result = recognize.getResult()) != null) {
                                    System.out.println("Running");
                                    String command = result.getHypothesis().trim();
                                    if(command.equalsIgnoreCase("PAUSE")||command.startsWith("PAUSE")||command.equalsIgnoreCase("PLAY")||command.startsWith("PLAY")){
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                pauseAndPlay();
                                            }
                                        });
                                    }
                                    else if(command.equalsIgnoreCase("NEXT")||command.startsWith("NEXT")){
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                next();
                                            }
                                        });
                                    }
                                    else if(command.equalsIgnoreCase("BACK")||command.startsWith("BACK")){
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                previous();
                                            }
                                        });
                                    }
                                    else if(command.equalsIgnoreCase("STOP")||command.startsWith("STOP")){
                                        Platform.runLater(new Runnable(){
                                            @Override
                                            public void run() {
                                                stopPlayer();
                                            }
                                        });
                                    }
                                    System.out.println(command);
                                    break;
                                }
                                recognize.stopRecognition();
                                recognize=null;
                                
                            } catch (IOException ex) {}
                            
                            new Thread(new VoiceThread()).start();
                            
                            System.out.println(line.getLineInfo());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stopListen();
                                }
                            });
                            
                        }
                        else{
                            if(prev==1)prev=0;
                        }
                        System.out.println("Result: "+map.get(0).getTranscript());
                    }
                    System.out.println(speechResults);
                }
            });

            System.out.println("Listening ");
            
        
        } 
        catch (LineUnavailableException e) {
            System.out.println("Line Unavailable");
        }
             return null;
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Glow glow=new Glow();
        glow.setLevel(0.9);
        hbox1.setEffect(glow);
        arr=new String[]{"C:\\Users\\Dakshin\\Downloads\\Tik Tik Tik - Title Track Lyric  Jayam Ravi, Nivetha Pethuraj  D.Imman.mp3","C:\\Users\\Dakshin\\Downloads\\twenty one pilots Heathens (from Suicide Squad The Album) [OFFICIAL VIDEO].mp3","C:\\Users\\Dakshin\\Downloads\\Michael Jackson - Smooth Criminal (Single Version) HD.mp3"};
        File file=new File(arr[0]);
        media = new Media(file.toURI().toString());
        player=new MediaPlayer(media);
        timer1.textProperty().bind(
                Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Duration currTime = player.getCurrentTime();
                return String.format("%02d:%02d", (int) currTime.toMinutes() % 60, (int) currTime.toSeconds() % 60);
            }
        }, player.currentTimeProperty()));
        timer3.textProperty().bind(
                Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Duration totTime = player.getTotalDuration();
                return String.format("%02d:%02d", (int) totTime.toMinutes() % 60, (int) totTime.toSeconds() % 60);
            }
        }, player.currentTimeProperty()));
        time.setPrefWidth(300);
        time.setId("timeSlider");
        time.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                if (time.isPressed()) {
                    player.seek(player.getMedia().getDuration().multiply(time.getValue() / 100));
                }
            }
        });
        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                updatesValues();
            }
        });
        vol.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                if (vol.isPressed()) {
                    player.setVolume(vol.getValue() / 100);
                    volume=(int)vol.getValue();
                    volumePercent.setText(String.valueOf(volume)+"%");
                }
            }
        });
        vol.setPrefWidth(70);
        vol.setValue(100);
        vol.setId("volSlider");
        functions.getChildren().add(vol);
        slider.getChildren().add(time);
        player.play();
        vt=new VoiceThread();
        Thread th=new Thread(vt);
        th.setDaemon(true);
        th.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    void pauseAndPlay() {
        Status status = player.getStatus(); 
        if (status == Status.PLAYING) { 
            if (player.getCurrentTime().greaterThanOrEqualTo(player.getTotalDuration())) { 
                player.seek(player.getStartTime());
                player.play(); 
            } 
            else { 
                player.pause(); 
            } 
        } 
        if (status == Status.HALTED || status == Status.STOPPED || status == Status.PAUSED) { 
            player.currentTimeProperty().addListener(new InvalidationListener() {
                @Override 
                public void invalidated(Observable ov) {
                    updatesValues();
                }
            }); 
            player.play();  
        } 
    }
    
    private void stopListen(){
        voiceStatus.setText("ON");
        for(int i=0;i<=volume;i+=20){
            player.setVolume(i/100.0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        player.setVolume(volume/100.0);
    }
    
    private void listen(){
        voiceStatus.setText("Listening");
        for(int i=volume;i>=0;i-=20){
            player.setVolume(i/100.0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        player.setVolume(0);
    }
    
     protected void updatesValues() { 
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                time.setValue(player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis() * 100);
            }
        }); 
    } 

    private void muteUnmute() {
        if(!mute){
            volume=(int)vol.getValue();
            vol.setValue(0);
            player.setVolume(0);
            volumePercent.setText("0%");
            mute=true;
            pre_volume=volume;
            volume=0;
        }
        else{
            volume=pre_volume;
            vol.setValue(volume);
            player.setVolume(volume/100);
            volumePercent.setText(volume+"%");
            mute=false;
        }
    }

    private void stopPlayer() {
        player.stop();
    }
    
    private void next(){
        songNo++;
        songNo%=3;
        player.stop();
        File file=new File(arr[songNo]);
        media = new Media(file.toURI().toString());
        player=new MediaPlayer(media);
        player.play();
        
        bind();
        
    }
    
    private void previous(){
        if(songNo>0)
            songNo--;
        player.stop();
        File file=new File(arr[songNo]);
        media = new Media(file.toURI().toString());
        player=new MediaPlayer(media);
        player.play();
        bind();
        
    }
    
    private void bind(){
        timer1.textProperty().bind(
                Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Duration currTime = player.getCurrentTime();
                return String.format("%02d:%02d", (int) currTime.toMinutes() % 60, (int) currTime.toSeconds() % 60);
            }
        }, player.currentTimeProperty()));
        timer3.textProperty().bind(
                Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Duration totTime = player.getTotalDuration();
                return String.format("%02d:%02d", (int) totTime.toMinutes() % 60, (int) totTime.toSeconds() % 60);
            }
        }, player.currentTimeProperty()));
        
        time.setPrefWidth(300);
        time.setId("timeSlider");
        time.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                if (time.isPressed()) {
                    player.seek(player.getMedia().getDuration().multiply(time.getValue() / 100));
                }
            }
        });
        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                updatesValues();
            }
        });
    }
}
