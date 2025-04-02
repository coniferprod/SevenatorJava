package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class KeyboardLevelScaling {
    public enum Style {
        LINEAR,
        EXPONENTIAL,
    };

    public enum Sign {
        NEGATIVE,
        POSITIVE,
    };

    public record Curve(Style style, Sign sign) { /* NO BODY */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(switch (sign) {
                case NEGATIVE -> '-';
                case POSITIVE -> '+';
            });
            sb.append(switch (style) {
                case LINEAR -> "LIN";
                case EXPONENTIAL -> "EXP";
            });
            return sb.toString();
        }

        public UInt8 toUInt8() {
            int value = switch (this.style) {
                case LINEAR -> switch (this.sign) {
                    case POSITIVE -> 3;
                    case NEGATIVE -> 0;
                };
                case EXPONENTIAL -> switch (this.sign) {
                    case POSITIVE -> 2;
                    case NEGATIVE -> 1;
                };
            };
            return new UInt8(value);
        }
    }

    public record Scaling(Level depth, Curve curve) { /*NO BODY*/
    }

    public Key breakpoint;
    public Scaling left;
    public Scaling right;

    public KeyboardLevelScaling() {
        this.breakpoint = new Key();
        this.left = new Scaling(new Level(0), new Curve(Style.LINEAR, Sign.NEGATIVE));
        this.right = new Scaling(new Level(0), new Curve(Style.LINEAR, Sign.NEGATIVE));
    }

    public static KeyboardLevelScaling parse(List<UInt8> data) throws ParseException {
        System.out.print("KLS data: "); UInt8.printList(data);
        KeyboardLevelScaling kls = new KeyboardLevelScaling();
        kls.breakpoint = new Key(data.get(0).value());

        int leftCurveValue = data.get(3).value();
        Curve leftCurve = switch (leftCurveValue) {
            case 0 -> new Curve(Style.LINEAR, Sign.NEGATIVE);
            case 1 -> new Curve(Style.EXPONENTIAL, Sign.NEGATIVE);
            case 2 -> new Curve(Style.EXPONENTIAL, Sign.POSITIVE);
            case 3 -> new Curve(Style.LINEAR, Sign.POSITIVE);
            default -> throw new ParseException("bad KLS curve value (left), offset = 3");
        };
        kls.left = new Scaling(new Level(data.get(1).value()), leftCurve);

        int rightCurveValue = data.get(4).value();
        Curve rightCurve = switch (rightCurveValue) {
            case 0 -> new Curve(Style.LINEAR, Sign.NEGATIVE);
            case 1 -> new Curve(Style.EXPONENTIAL, Sign.NEGATIVE);
            case 2 -> new Curve(Style.EXPONENTIAL, Sign.POSITIVE);
            case 3 -> new Curve(Style.LINEAR, Sign.POSITIVE);
            default -> throw new ParseException("bad KLS curve value (right), offset 4");
        };
        kls.right = new Scaling(new Level(data.get(2).value()), rightCurve);

        return kls;
    }

    public List<UInt8> toData() {
        List<UInt8> result = new ArrayList<>();

        result.add(new UInt8(this.breakpoint.value()));
        result.add(new UInt8(this.left.depth.value()));
        result.add(new UInt8(this.right.depth.value()));
        result.add(this.left.curve.toUInt8());
        result.add(this.right.curve.toUInt8());

        return result;
    }

    public Element toXML(Document document) {
        Element element = document.createElement("keyboardLevelScaling");

        element.setAttribute("breakpoint", Integer.toString(this.breakpoint.value()));

        Element depthElement = document.createElement("depth");
        depthElement.setAttribute("left", Integer.toString(this.left.depth.value()));
        depthElement.setAttribute("right", Integer.toString(this.right.depth.value()));
        element.appendChild(depthElement);

        Element curveElement = document.createElement("curve");
        curveElement.setAttribute("left", this.left.curve.toString());
        curveElement.setAttribute("right", this.right.curve.toString());
        element.appendChild(curveElement);

        return element;
    }
}
