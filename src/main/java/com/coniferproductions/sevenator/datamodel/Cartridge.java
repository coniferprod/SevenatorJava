package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public final class Cartridge {
    public static final int DATA_SIZE = 4096;

    public static final int VOICE_COUNT = 32;
    public List<Voice> voices;

    public Cartridge() {
        this.voices = new ArrayList<>();

        for (int i = 0; i < VOICE_COUNT; i++) {
            this.voices.add(new Voice());
        }
    }

    public static Cartridge parse(List<UInt8> data) throws ParseException {
        if (data.size() != DATA_SIZE) {
            throw new ParseException(
                    String.format("Error in data size, expecting %d bytes, got %d", DATA_SIZE, data.size()));
        }

        //System.out.print("cartridge data = "); UInt8.printList(data.subList(0, 32));
        Cartridge cartridge = new Cartridge();

        int offset = 0;
        for (int i = 0; i < VOICE_COUNT; i++) {
            List<UInt8> packedVoiceData = data.subList(offset, offset + Voice.PACKED_DATA_SIZE);
            List<UInt8> voiceData = Voice.unpack(packedVoiceData);
            //System.out.print("offset = " + offset + ", voice " + (i + 1) + " data ");
            Voice voice = Voice.parse(voiceData);
            cartridge.voices.set(i, voice);
            offset += Voice.PACKED_DATA_SIZE;
        }

        return cartridge;
    }

    public String toXML() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("cartridge");

            Element voicesElement = doc.createElement("voices");
            for (Voice voice : this.voices) {
                voicesElement.appendChild(voice.toXML(doc));
            }
            rootElement.appendChild(voicesElement);

            doc.appendChild(rootElement);

            // Construct a do-nothing transformation
            Transformer t = TransformerFactory.newInstance().newTransformer();

            // Set output properties to get a DOCTYPE node
            //t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemIdentifier);
            //t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicIdentifier);
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");

            StringWriter writer = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(writer));
            //String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            String output = writer.getBuffer().toString();
            return output;
        } catch (ParserConfigurationException pce) {
            System.err.println("Unable to get a document builder, error: " + pce.getLocalizedMessage());
            return "";
        } catch (TransformerConfigurationException tce) {
            System.err.println("Unable to get a transformer, error: " + tce.getLocalizedMessage());
            return "";
        } catch (TransformerException te) {
            System.err.println("Error in transformation: " + te.getLocalizedMessage());
            return "";
        }
    }

    public List<UInt8> toData() {
        List<UInt8> result = new ArrayList<>();

        int index = 0;
        for (Voice voice : this.voices) {
            var voiceData = voice.toData();
            var packedVoiceData = Voice.pack(voiceData);
            result.addAll(packedVoiceData);
        }

        assert result.size() == DATA_SIZE;
        return result;
    }
}
