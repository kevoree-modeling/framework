package org.kevoree.modeling.format.xmi;

import java.util.ArrayList;

public class XmlParser {

    private String payload;
    private int current = 0;
    private char currentChar;
    private String tagName, tagPrefix, attributePrefix;
    private boolean readSingleton = false;

    private ArrayList<String> attributesNames = new ArrayList<String>();
    private ArrayList<String> attributesPrefixes = new ArrayList<String>();
    private ArrayList<String> attributesValues = new ArrayList<String>();

    private StringBuilder attributeName = new StringBuilder();
    private StringBuilder attributeValue = new StringBuilder();

    public XmlParser(String str) {
        this.payload = str;
        currentChar = readChar();
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public Boolean hasNext() {
        read_lessThan();
        return current < payload.length();
    }

    public String getLocalName() {
        return tagName;
    }

    public int getAttributeCount() {
        return attributesNames.size();
    }

    public String getAttributeLocalName(int i) {
        return attributesNames.get(i);
    }

    public String getAttributePrefix(int i) {
        return attributesPrefixes.get(i);
    }

    public String getAttributeValue(int i) {
        return attributesValues.get(i);
    }

    private char readChar() {
        if (current < payload.length()) {
            char re = payload.charAt(current);
            current++;
            return re;
        }
        return '\0';
    }

    public XmlToken next() {

        if (readSingleton) {
            readSingleton = false;
            return XmlToken.END_TAG;
        }

        if (!hasNext()) {
            return XmlToken.END_DOCUMENT;
        }

        attributesNames.clear();
        attributesPrefixes.clear();
        attributesValues.clear();

        read_lessThan(); // trim to the begin of a tag
        currentChar = readChar(); //inputStream.read().toChar()

        if (currentChar == '?') { // XML header <?xml version="1.0" encoding="UTF-8"?>
            currentChar = readChar();
            read_xmlHeader();
            return XmlToken.XML_HEADER;

        } else if (currentChar == '!') { // XML comment <!-- xml version="1.0" encoding="UTF-8" -->
            do {
                currentChar = readChar();
            } while (currentChar != '>');
            return XmlToken.COMMENT;

        } else if (currentChar == '/') { // XML closing tag </tagname>
            currentChar = readChar();
            read_closingTag();
            return XmlToken.END_TAG;
        } else {
            read_openTag();
            if (currentChar == '/') {
                read_upperThan();
                readSingleton = true;
            }
            return XmlToken.START_TAG;
        }
    }

    private void read_lessThan() {
        while (currentChar != '<' && currentChar != '\0') {
            currentChar = readChar();
        }
    }

    private void read_upperThan() {
        while (currentChar != '>') {
            currentChar = readChar();
        }
    }

    /**
     * Reads XML header <?xml version="1.0" encoding="UTF-8"?>
     */
    private void read_xmlHeader() {
        read_tagName();
        read_attributes();
        read_upperThan();
    }


    private void read_closingTag() {
        read_tagName();
        read_upperThan();
    }

    private void read_openTag() {
        read_tagName();
        if (currentChar != '>' && currentChar != '/') {
            read_attributes();
        }
    }

    private void read_tagName() {
        tagName = "" + currentChar;
        tagPrefix = null;
        currentChar = readChar();
        while (currentChar != ' ' && currentChar != '>' && currentChar != '/') {
            if (currentChar == ':') {
                tagPrefix = tagName;
                tagName = "";
            } else {
                tagName += currentChar;
            }
            currentChar = readChar();
        }
    }

    private void read_attributes() {

        boolean end_of_tag = false;

        while (currentChar == ' ') {
            currentChar = readChar();
        }
        while (!end_of_tag) {
            while (currentChar != '=') { // read attributeName and/or prefix
                if (currentChar == ':') {
                    attributePrefix = attributeName.toString();
                    attributeName = new StringBuilder();
                    // attributeName.delete(0, attributeName.length())
                } else {
                    attributeName.append(currentChar);
                }
                currentChar = readChar();
            }
            do {
                currentChar = readChar();
            } while (currentChar != '"');
            currentChar = readChar();
            while (currentChar != '"') { // reading value
                attributeValue.append(currentChar);
                currentChar = readChar();
            }

            attributesNames.add(attributeName.toString());
            attributesPrefixes.add(attributePrefix);
            attributesValues.add(attributeValue.toString());
            attributeName = new StringBuilder();
            //attributeName.delete(0, attributeName.length())
            attributePrefix = null;
            //attributeValue.delete(0, attributeValue.length())
            attributeValue = new StringBuilder();

            do {//Trim to next attribute
                currentChar = readChar();
                if (currentChar == '?' || currentChar == '/' || currentChar == '-' || currentChar == '>') {
                    end_of_tag = true;
                }
            } while (!end_of_tag && currentChar == ' ');

        }

    }

}






