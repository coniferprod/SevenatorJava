package com.coniferproductions.sevenator;

import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.datamodel.Voice;
import com.coniferproductions.sevenator.datamodel.Octave;
import com.coniferproductions.sevenator.datamodel.ParseException;
import com.coniferproductions.sevenator.sysex.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteOrder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static java.lang.System.Logger.Level.*;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javafx.embed.swing.SwingNode;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class App extends Application {
    public static final String LOGGER_NAME = "com.coniferproductions.sevenator";
    private static System.Logger logger = System.getLogger(LOGGER_NAME);

    private Cartridge cartridge;
    private ListView<String> listView;
    ObservableList<String> voiceNameList;

    public App() {
        this.cartridge = new Cartridge();
        this.voiceNameList = FXCollections.observableArrayList();
        List<String> voiceNames = new ArrayList<>();
        for (Voice voice : this.cartridge.voices) {
            voiceNames.add(voice.name.toString());
        }
        voiceNameList.addAll(voiceNames);
        this.listView = new ListView<String>(voiceNameList);
    }

    private void populateVoiceList() {
        this.voiceNameList.clear();
        List<String> voiceNames = new ArrayList<>();
        for (Voice voice : this.cartridge.voices) {
            voiceNames.add(voice.name.toString());
        }
        voiceNameList.addAll(voiceNames);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, TranscoderException {
        populateVoiceList();

        VBox vbox = new VBox(this.listView);

        SplitPane splitPane = new SplitPane();

        StackPane stackPane = new StackPane();

        //VBox rightControl = new VBox(new Label("Right Control"));

        // Create an SVGConverter instance with the path to the SVG file.
        SVGConverter converter = new SVGConverter(Paths.get(System.getProperty("user.home") + "/tmp/dx7algsvg", "alg01.svg").toString());
        // Read from resources when packaged as JAR.

        // Convert the SVG to a JavaFX Image
        Image image = converter.toImage();

        // Display the SVG image using an ImageView
        ImageView imageView = new ImageView(image);

        // Add the ImageView to the parent layout
        stackPane.getChildren().add(imageView);

        splitPane.getItems().addAll(vbox, stackPane);

        BorderPane borderPane = new BorderPane();

        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
        Menu fileMenu = makeFileMenu(primaryStage);
        menuBar.getMenus().add(fileMenu);

        borderPane.setTop(menuBar);
        borderPane.setCenter(splitPane);

        HBox statusBar = new HBox();
        borderPane.setBottom(statusBar);

        Scene scene = new Scene(borderPane, 1024, 768);

        primaryStage.setTitle("Sevenator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Menu makeFileMenu(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MIDI System Exclusive Files", "*.syx"),
                new FileChooser.ExtensionFilter("XML Cartridge Description Files", "*.xml")
        );

        final Menu fileMenu = new Menu("File");

        MenuItem newMenuItem = new MenuItem("New");

        MenuItem openMenuItem = new MenuItem("Open...");
        openMenuItem.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            System.out.println("File | Open selected, file = " + selectedFile.getPath());

            Path path = Path.of(selectedFile.getPath());
            List<UInt8> data = new ArrayList<>();
            try {
                byte[] contents = Files.readAllBytes(path);
                data = UInt8.listFromByteArray(contents);

                Message message = Message.parse(data);
                logger.log(DEBUG, "data length = " + data.size());

                Header header = Header.parse(message.getPayload());
                logger.log(DEBUG, header);

                List<UInt8> payload = message.getPayload();
                List<UInt8> cartridgeData = payload.subList(header.getDataSize(), payload.size() - 1);

                cartridge = Cartridge.parse(cartridgeData);
                populateVoiceList();
            } catch (IOException ioe) {
                System.err.println("Error reading file: " + ioe.getLocalizedMessage());
            } catch (ParseException pe) {
                System.err.println("Parse error: " + pe.getMessage());
                System.exit(1);
            }
        });

        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(e -> {
            File selectedFile = fileChooser.showSaveDialog(stage);
            System.out.println("File | Save selected, file = " + selectedFile.getPath());

            byte[] fileData = UInt8.byteArrayFromList(this.cartridge.toData());

            Path path = Path.of(selectedFile.getPath());
            try {
                Files.write(path, fileData);
            } catch (IOException ex) {
                System.err.println("Error writing file: " + ex.getLocalizedMessage());
            }
        });

        MenuItem exportMenuItem = new MenuItem("Export to XML");
        exportMenuItem.setOnAction(e -> {
            File selectedFile = fileChooser.showSaveDialog(stage);
            System.out.println("File | Export to XML selected, file = " + selectedFile.getPath());

            String xml = this.cartridge.toXML();
            //System.out.println(xml);
            try {
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(selectedFile));
                writer.write(xml);
                writer.close();
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error writing XML to file: " + fnfe.getLocalizedMessage());
            } catch (IOException ioe) {
                System.err.println("Error writing XML to file: " + ioe.getLocalizedMessage());
            }
        });

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> {
            Platform.exit();
        });

        fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem, exportMenuItem,
                new SeparatorMenuItem(), exitMenuItem);
        return fileMenu;
    }

    public static void main(String[] args) {
        launch();

        /*
        List<UInt8> data = new ArrayList<>();
        try {
            byte[] contents = Files.readAllBytes(Paths.get(args[0]));
            data = UInt8.listFromByteArray(contents);
        } catch (IOException ioe) {
            System.err.println(ioe.getLocalizedMessage());
            ioe.printStackTrace();
            System.exit(1);
        }

        Message message = Message.parse(data);
        logger.log(DEBUG, "data length = " + data.size());

        Header header = Header.parse(message.getPayload());
        logger.log(DEBUG, header);

        List<UInt8> payload = message.getPayload();
        List<UInt8> cartridgeData = payload.subList(header.getDataSize(), payload.size() - 1);

        try {
            //UInt8.printList(cartridgeData);
            Cartridge cartridge = Cartridge.parse(cartridgeData);

            String xml = cartridge.toXML();
            System.out.println(xml);

            List<UInt8> outputData = cartridge.toData();
            assert outputData.size() == Cartridge.DATA_SIZE;

            List<UInt8> outputPayload = new ArrayList<>();
            Header outputHeader = new Header(new Channel(1), Header.Format.CARTRIDGE);
            outputPayload.addAll(outputHeader.toData());
            outputPayload.addAll(outputData);
            outputPayload.add(UInt8.checksum(outputData));
            Message outputMessage = new Message(Manufacturer.YAMAHA, outputPayload);
            byte[] messageData = UInt8.byteArrayFromList(outputMessage.toData());
            Files.write(Paths.get("javaouttest.syx"), messageData);
        } catch (ParseException pe) {
            System.err.println("Parse error: " + pe.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Error writing output file: " + ioe.getMessage());
            System.exit(1);
        }
        */

    }

    public static void test(String arg0) {
        System.out.println("System byte order = " + ByteOrder.nativeOrder());

        Channel channel = new Channel(1);
        System.out.println("Channel value = " + channel);
        System.out.println(String.format("Channel = [%d, %d]", Channel.TYPE.first(), Channel.TYPE.last()));
        System.out.println("Channel contains -7? " + Channel.TYPE.contains(-7));
        System.out.println("Channel contains 10? " + Channel.TYPE.contains(10));

        Channel channel1 = new Channel(1);
        Channel channel2 = new Channel(2);
        if (channel1.equals(channel2)) {
            System.out.println("Same channel.");
        } else {
            System.out.println("Different channel.");
        }

        MIDINote middleC = new MIDINote(60);
        System.out.println("Middle C is MIDI note number " + middleC.value()
            + ", or " + middleC);

        for (int value = middleC.last(); value >= middleC.first(); value--) {
            MIDINote note = new MIDINote(value);
            MIDINote.octave = Octave.ROLAND;
            System.out.print(value + " = " + note + " / ");
            MIDINote.octave = Octave.YAMAHA;
            System.out.println(note);
        }
        System.out.println();

        List<UInt8> data = new ArrayList<>();
        try {
            byte[] contents = Files.readAllBytes(Paths.get(arg0));
            System.out.printf("%02X ... %02X", contents[0], contents[contents.length - 1]);

            data = UInt8.listFromByteArray(contents);
            Message message = Message.parse(data);
            System.out.printf("%nMessage information:%n");
            System.out.printf("payload = %d bytes%n%n", message.getPayload().size());

            Header header = Header.parse(message.getPayload());
            System.out.println("Header: format = " + header.getFormat());
            System.out.println("byte count = " + header.getByteCount());
            System.out.println("channel = " + header.getChannel());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.printf("RangedInteger subclass instances created: %d%n", RangedInteger.getCount());

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        System.out.printf("Total memory: %10d%n", totalMemory);
        System.out.printf("Free memory:  %10d%n", freeMemory);
        System.out.printf("Used memory:  %10d%n", usedMemory);
    }
}

class SVGConverter extends ImageTranscoder {
    private BufferedImage image;

    public SVGConverter(String svgFilePath) throws TranscoderException {
        // Initialize the SVGConverter with the path to the SVG file
        TranscoderInput input = new TranscoderInput(svgFilePath);
        this.transcode(input, null);
    }

    public BufferedImage toBufferedImage() {
        return this.image;
    }

    public Image toImage() {
        // Convert the BufferedImage to a JavaFX Image
        return SwingFXUtils.toFXImage(this.toBufferedImage(), null);
    }

    @Override
    public BufferedImage createImage(int width, int height) {
        // Create a new BufferedImage with the specified width and height
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {
        // Store the converted image in the 'image' variable
        image = img;
    }
}

// SVG stuff is adapted from https://coderscratchpad.com/displaying-svg-images-in-javafx/
