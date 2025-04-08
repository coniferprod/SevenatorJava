package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.coniferproductions.sevenator.App.LOGGER_NAME;
import static java.lang.System.Logger.Level.*;

public final class Cartridge {
    public static final int DATA_SIZE = 4096;

    public static final int VOICE_COUNT = 32;
    public List<Voice> voices;

    public List<Voice> getVoices() {
        return voices;
    }

    public void setVoices(List<Voice> voices) {
        this.voices = voices;
    }

    public Cartridge() {
        this.voices = new ArrayList<>();

        for (int i = 0; i < VOICE_COUNT; i++) {
            this.voices.add(new Voice());
        }
    }

    public static System.Logger logger = System.getLogger(LOGGER_NAME);

    public static Cartridge parse(Document document) {
        Cartridge cartridge = new Cartridge();

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("//voice");

            Object result = expr.evaluate(document, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            for (int i = 0; i < nodes.getLength(); i++) {
                Voice voice = new Voice();

                Node voiceNode = nodes.item(i);
                NamedNodeMap voiceAttrs = voiceNode.getAttributes();

                voice.name = new VoiceName(voiceAttrs.getNamedItem("name").getNodeValue());
                voice.algorithm = new Algorithm(Integer.parseInt(voiceAttrs.getNamedItem("algorithm").getNodeValue()));
                voice.transpose = new Transpose(Integer.parseInt(voiceAttrs.getNamedItem("transpose").getNodeValue()));
                voice.feedback = new Depth(Integer.parseInt(voiceAttrs.getNamedItem("feedback").getNodeValue()));
                voice.oscSync = Boolean.parseBoolean(voiceAttrs.getNamedItem("oscillatorSync").getNodeValue());
                voice.pitchModulationSensitivity = new Depth(Integer.parseInt(voiceAttrs.getNamedItem("pitchModulationSensitivity").getNodeValue()));

                List<Operator> operators = new ArrayList<>();

                for (int op = 0; op < Voice.OPERATOR_COUNT; op++) {
                    String pathBase = String.format("//voice[%d]/operators/operator[%d]", i + 1, op + 1);
                    StringBuffer pathBuffer = new StringBuffer(pathBase);

                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    Node opNode = (Node) expr.evaluate(document, XPathConstants.NODE);
                    //System.out.println(opNode.getNodeName() + "" + (op + 1));

                    pathBuffer.append("/eg");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    Node egNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.append("/rates/text()");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    String egRatesText = (String) expr.evaluate(document, XPathConstants.STRING);

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/eg/levels/text()");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    String egLevelsText = (String) expr.evaluate(document, XPathConstants.STRING);

                    operators.add(getOperatorFromXml(opNode, egRatesText, egLevelsText));

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/keyboardLevelScaling");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    Node klsNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.append("/depth");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    Node depthNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/keyboardLevelScaling/curve");
                    logger.log(INFO, "path = " + pathBuffer.toString());
                    expr = xpath.compile(pathBuffer.toString());
                    Node curveNode = (Node) expr.evaluate(document, XPathConstants.NODE);
                }

                voice.setOperators(operators);

                String pathBase = String.format("//voice[%d]/peg", i + 1);
                StringBuffer pathBuffer = new StringBuffer(pathBase);

                pathBuffer.append("/rates");
                logger.log(INFO, "path = " + pathBuffer.toString());
                expr = xpath.compile(pathBuffer.toString());
                String pegRatesText = (String) expr.evaluate(document, XPathConstants.STRING);

                pathBuffer.setLength(0);
                pathBuffer.append(pathBase + "/levels");
                logger.log(INFO, "path = " + pathBuffer.toString());
                expr = xpath.compile(pathBuffer.toString());
                String pegLevelsText = (String) expr.evaluate(document, XPathConstants.STRING);

                voice.peg = getEgFromXml(pegRatesText, pegLevelsText);

                expr = xpath.compile(String.format("//voice[%d]/lfo", (i + 1)));
                Node lfoNode = (Node) expr.evaluate(document, XPathConstants.NODE);
                voice.lfo = getLfoFromXml(lfoNode);

                cartridge.voices.add(voice);
            }

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return cartridge;
    }

    private static Operator getOperatorFromXml(Node opNode, String ratesText, String levelsText) {
        Operator op = new Operator();

        NamedNodeMap attrs = opNode.getAttributes();
        Node attrNode = attrs.getNamedItem("level");
        op.outputLevel = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("mode");
        if (attrNode.getNodeValue().equals("ratio")) {
            op.mode = Operator.Mode.RATIO;
        } else {
            op.mode = Operator.Mode.FIXED;
        }

        attrNode = attrs.getNamedItem("coarse");
        op.coarse = new Coarse(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("fine");
        op.fine = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("detune");
        op.detune = new Detune(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("amplitudeModulationSensitivity");
        op.amplitudeModulationSensitivity = new Sensitivity(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("keyVelocitySensitivity");
        op.keyVelocitySensitivity = new Depth(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("keyboardRateScaling");
        op.keyboardRateScaling = new Depth(Integer.parseInt(attrNode.getNodeValue()));

        op.eg = getEgFromXml(ratesText, levelsText);
        return op;
    }

    private static Envelope getEgFromXml(String ratesText, String levelsText) {
        Envelope eg = new Envelope();

        //String ratesValue = rates.getNodeValue();
        String[] parts = ratesText.split(" ");
        for (int i = 0; i < parts.length; i++) {
            eg.rates.add(new Rate(Integer.parseInt(parts[i])));
        }

        //String levelsValue = levels.getNodeValue();
        parts = levelsText.split(" ");
        for (int i = 0; i < parts.length; i++) {
            eg.levels.add(new Level(Integer.parseInt(parts[i])));
        }

        return eg;
    }

    private static LFO getLfoFromXml(Node node) {
        LFO lfo = new LFO();

        NamedNodeMap attrs = node.getAttributes();
        Node attrNode = attrs.getNamedItem("speed");

        lfo.speed = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("delay");
        lfo.delay = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("pmd");
        lfo.pmd = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("amd");
        lfo.amd = new Level(Integer.parseInt(attrNode.getNodeValue()));

        attrNode = attrs.getNamedItem("sync");
        lfo.sync = Boolean.parseBoolean(attrNode.getNodeValue());

        attrNode = attrs.getNamedItem("wave");
        lfo.waveform = switch (attrNode.getNodeValue()) {
            case "saw-down" -> LFO.Waveform.SAW_DOWN;
            case "saw-up" -> LFO.Waveform.SAW_UP;
            case "triangle" -> LFO.Waveform.TRIANGLE;
            case "square" -> LFO.Waveform.SQUARE;
            case "sine" -> LFO.Waveform.SINE;
            case "sample-and-hold" -> LFO.Waveform.SAMPLE_AND_HOLD;
            default -> LFO.Waveform.SINE;
        };

        return lfo;
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
