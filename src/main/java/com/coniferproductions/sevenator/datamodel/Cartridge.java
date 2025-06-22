package com.coniferproductions.sevenator.datamodel;

import java.io.StringWriter;
import static java.lang.System.Logger.Level.DEBUG;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.coniferproductions.sevenator.App.LOGGER_NAME;
import com.coniferproductions.sevenator.sysex.UInt7;

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

    public Cartridge(List<Voice> voices) {
        this.voices = voices;
    }

    public static System.Logger logger = System.getLogger(LOGGER_NAME);

    public static Cartridge parse(Document document) throws ParseException {
        List<Voice> voices = new ArrayList<>();

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("//voice");

            Object result = expr.evaluate(document, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            for (int voiceIndex = 1; voiceIndex <= nodes.getLength(); voiceIndex++) {
                Voice voice = new Voice();

                Node voiceNode = nodes.item(voiceIndex - 1);
                NamedNodeMap voiceAttrs = voiceNode.getAttributes();

                voice.name = new VoiceName(voiceAttrs.getNamedItem("name").getNodeValue());
                voice.algorithm = new Algorithm(Integer.parseInt(voiceAttrs.getNamedItem("algorithm").getNodeValue()));
                voice.transpose = new Transpose(Integer.parseInt(voiceAttrs.getNamedItem("transpose").getNodeValue()));
                voice.feedback = new Depth(Integer.parseInt(voiceAttrs.getNamedItem("feedback").getNodeValue()));
                voice.oscSync = Boolean.parseBoolean(voiceAttrs.getNamedItem("oscillatorSync").getNodeValue());
                voice.pitchModulationSensitivity = new Depth(Integer.parseInt(voiceAttrs.getNamedItem("pitchModulationSensitivity").getNodeValue()));

                Map<OperatorIndex, Operator> operators = new HashMap<>();

                for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
                    String pathBase = String.format("//voice[%d]/operators/operator[%d]", voiceIndex, index);
                    StringBuilder pathBuffer = new StringBuilder(pathBase);

                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    Node opNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.append("/eg");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    Node egNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.append("/rates/text()");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    String egRatesText = (String) expr.evaluate(document, XPathConstants.STRING);

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/eg/levels/text()");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    String egLevelsText = (String) expr.evaluate(document, XPathConstants.STRING);

                    Operator op = getOperatorFromXml(opNode, egRatesText, egLevelsText);

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/keyboardLevelScaling");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    Node klsNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.append("/depth");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    Node depthNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    pathBuffer.setLength(0);
                    pathBuffer.append(pathBase + "/keyboardLevelScaling/curve");
                    logger.log(DEBUG, "path = " + pathBuffer);
                    expr = xpath.compile(pathBuffer.toString());
                    Node curveNode = (Node) expr.evaluate(document, XPathConstants.NODE);

                    op.keyboardLevelScaling = getKlsFromXml(klsNode, depthNode, curveNode);
                    operators.put(new OperatorIndex(index), op);
                }

                voice.setOperators(operators);

                String pathBase = String.format("//voice[%d]/peg", voiceIndex);
                StringBuffer pathBuffer = new StringBuffer(pathBase);

                pathBuffer.append("/rates");
                logger.log(DEBUG, "path = " + pathBuffer);
                expr = xpath.compile(pathBuffer.toString());
                String pegRatesText = (String) expr.evaluate(document, XPathConstants.STRING);

                pathBuffer.setLength(0);
                pathBuffer.append(pathBase + "/levels");
                logger.log(DEBUG, "path = " + pathBuffer);
                expr = xpath.compile(pathBuffer.toString());
                String pegLevelsText = (String) expr.evaluate(document, XPathConstants.STRING);

                voice.peg = getEgFromXml(pegRatesText, pegLevelsText);

                expr = xpath.compile(String.format("//voice[%d]/lfo", voiceIndex));
                Node lfoNode = (Node) expr.evaluate(document, XPathConstants.NODE);
                voice.lfo = getLfoFromXml(lfoNode);

                voices.add(voice);
            }

        } catch (XPathExpressionException e) {
            throw new ParseException("Error parsing XML: " + e.getMessage());
        }

        return new Cartridge(voices);
    }

    private static KeyboardLevelScaling getKlsFromXml(Node klsNode, Node depthNode, Node curveNode) {
        KeyboardLevelScaling kls = new KeyboardLevelScaling();

        NamedNodeMap attrs = klsNode.getAttributes();
        kls.breakpoint = new Key(Integer.parseInt(attrs.getNamedItem("breakpoint").getNodeValue()));

        attrs = depthNode.getAttributes();
        Level leftDepth = new Level(Integer.parseInt(attrs.getNamedItem("left").getNodeValue()));
        Level rightDepth = new Level(Integer.parseInt(attrs.getNamedItem("right").getNodeValue()));

        attrs = curveNode.getAttributes();
        String leftCurveValue = attrs.getNamedItem("left").getNodeValue();
        KeyboardLevelScaling.Curve leftCurve = switch (leftCurveValue) {
            case "-LIN" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.NEGATIVE);
            case "+LIN" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.POSITIVE);
            case "-EXP" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.EXPONENTIAL, KeyboardLevelScaling.Sign.NEGATIVE);
            case "+EXP" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.EXPONENTIAL, KeyboardLevelScaling.Sign.POSITIVE);
            default ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.NEGATIVE);
        };

        String rightCurveValue = attrs.getNamedItem("right").getNodeValue();
        KeyboardLevelScaling.Curve rightCurve = switch (rightCurveValue) {
            case "-LIN" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.NEGATIVE);
            case "+LIN" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.POSITIVE);
            case "-EXP" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.EXPONENTIAL, KeyboardLevelScaling.Sign.NEGATIVE);
            case "+EXP" ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.EXPONENTIAL, KeyboardLevelScaling.Sign.POSITIVE);
            default ->
                    new KeyboardLevelScaling.Curve(KeyboardLevelScaling.Style.LINEAR, KeyboardLevelScaling.Sign.NEGATIVE);
        };

        kls.left = new KeyboardLevelScaling.Scaling(leftDepth, leftCurve);
        kls.right = new KeyboardLevelScaling.Scaling(rightDepth, rightCurve);

        return kls;
    }

    private static Operator getOperatorFromXml(Node opNode, String ratesText, String levelsText) {
        Operator op = new Operator();

        NamedNodeMap attrs = opNode.getAttributes();
        Node attrNode = attrs.getNamedItem("level");
        op.level = new Level(Integer.parseInt(attrNode.getNodeValue()));

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

        String[] parts = ratesText.split(" ");
        List<Rate> rates = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            rates.add(new Rate(Integer.parseInt(parts[i])));
        }

        parts = levelsText.split(" ");
        List<Level> levels = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            levels.add(new Level(Integer.parseInt(parts[i])));
        }

        return new Envelope(rates, levels);
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

    public static Cartridge parse(List<UInt7> data) throws ParseException {
        if (data.size() != DATA_SIZE) {
            throw new ParseException(
                    String.format("Error in data size, expecting %d bytes, got %d", DATA_SIZE, data.size()));
        }

        //System.out.print("cartridge data = "); UInt8.printList(data.subList(0, 32));
        Cartridge cartridge = new Cartridge();

        int offset = 0;
        for (int i = 0; i < VOICE_COUNT; i++) {
            List<UInt7> packedVoiceData = data.subList(offset, offset + Voice.PACKED_DATA_SIZE);
            List<UInt7> voiceData = Voice.unpack(packedVoiceData);
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

    public List<UInt7> toData() {
        List<UInt7> result = new ArrayList<>();

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
