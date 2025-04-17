package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.UInt8;
import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.datamodel.ParseException;
import com.coniferproductions.sevenator.datamodel.Voice;
import com.coniferproductions.sevenator.sysex.Header;
import com.coniferproductions.sevenator.sysex.Loader;
import com.coniferproductions.sevenator.sysex.Message;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.SchemaFactoryConfigurationError;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static com.coniferproductions.sevenator.App.LOGGER_NAME;
import static java.lang.System.Logger.Level.*;
import static java.lang.System.Logger.Level.ERROR;

@Command(name = "dump")
public class Dump implements Runnable {
    private static System.Logger logger = System.getLogger(LOGGER_NAME);

    @CommandLine.Parameters(arity = "1", paramLabel = "INPUTFILE")
    Path inputFile;

    @Override
    public void run() {
        System.out.println("inputFile = " + inputFile.toString());

        Cartridge cartridge = Loader.loadCartridge(inputFile);

        for (Voice voice : cartridge.voices) {
            System.out.println(voice.name);
        }
    }

    private Cartridge cartridge;

    private void loadCartridge(Path path) {
        String filename = path.getFileName().toString();
        String extension = getFileExtension(filename);
        if (extension.equals("syx")) {
            try {
                loadSyxFile(path);
            } catch (IOException ioe) {
                System.err.println("Error reading file: " + ioe.getLocalizedMessage());
            } catch (ParseException pe) {
                System.err.println("Parse error: " + pe.getMessage());
                System.exit(1);
            }
        } else if (extension.equals("xml")) {
            try {
                loadXmlDocument(path);

            } catch (ParserConfigurationException | IOException ex) {
                throw new RuntimeException(ex);
            } catch (SAXException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void loadSyxFile(Path path) throws IOException, ParseException {
        byte[] contents = Files.readAllBytes(path);
        List<UInt8> data = UInt8.listFromByteArray(contents);

        Message message = Message.parse(data);
        logger.log(DEBUG, "data length = " + data.size());
        System.out.println("message data length = " + data.size());

        Header header = Header.parse(message.getPayload());
        logger.log(DEBUG, header);
        System.out.println("header = " + header);

        List<UInt8> payload = message.getPayload();
        System.out.println("payload size = " + payload.size());
        int dataStart = header.getDataSize();
        int dataEnd = payload.size() - 1;
        System.out.println("cartridge data is " + dataStart + " .. " + dataEnd);
        List<UInt8> cartridgeData = payload.subList(header.getDataSize(), payload.size() - 1);
        System.out.println("cartridge data size = " + cartridgeData.size());

        this.cartridge = Cartridge.parse(cartridgeData);
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private void loadXmlDocument(Path path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setNamespaceAware(true);  // required for validation

        // The validation docs say that "You should not both set a schema
        // and call setValidating(true) on a parser factory"
        //documentBuilderFactory.setValidating(true);

        documentBuilderFactory.setIgnoringElementContentWhitespace(true);

        // Prepare the documentBuilderFactory for handling schemas
        final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);

        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                logger.log(WARNING, exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                logger.log(ERROR, exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                logger.log(ERROR, exception.getMessage());
            }
        });
        Document document = builder.parse(Files.newInputStream(path));

        // Load the schema for validation
        try {
            // Create a SchemaFactory capable of understanding XML Schemas
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load XML Schema, represented by a Schema instance
            Source schemaFile = new StreamSource(
                    new File(Paths.get(System.getProperty("user.home") + "/tmp/", "cartridge.xsd").toString()));
            Schema schema = schemaFactory.newSchema(schemaFile);

            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            logger.log(INFO, path + " validated successfully");

            document.getDocumentElement().normalize();
            cartridge = Cartridge.parse(document);
        } catch (SchemaFactoryConfigurationError sfce) {
            logger.log(ERROR, sfce.getMessage());
        } catch (ParseException pe) {
            logger.log(ERROR, pe.getMessage());
        }
    }

}
