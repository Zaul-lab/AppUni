package it.universita.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigHandler extends DefaultHandler {

    private final StringBuilder buffer = new StringBuilder();

    private String serverHost;
    private Integer serverPort;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private String clientHost;
    private Integer clientPort;
    private Integer clientTimeout;

    @Override
    // Questo metodo viene invocato quando SAX incontra un tag di apertura.
    // Qui ci prepariamo a leggere il contenuto del nuovo elemento svuotando il buffer.
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        buffer.setLength(0); // puliamo il buffer per nuovo tag
    }

    @Override
    // Questo metodo viene invocato quando SAX trova del testo dentro un elemento.
    // Può essere chiamato più volte per lo stesso tag, quindi accumuliamo il testo nel buffer.
    public void characters(char[] ch, int start, int length) {
        buffer.append(ch, start, length);
    }

    @Override
    // Questo metodo viene invocato alla chiusura di un tag.
    // A questo punto il buffer contiene tutto il testo di quell'elemento:
    // in base al nome del tag (qName) decidiamo in quale variabile salvarlo.
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String text = buffer.toString().trim();

        switch (qName) {
            case "host":
                serverHost = text;
                break;
            case "port":
                serverPort = Integer.parseInt(text);
                break;

            case "url":
                dbUrl = text;
                break;
            case "user":
                dbUser = text;
                break;
            case "password":
                dbPassword = text;
                break;

            case "clientHost":
                clientHost = text;
                break;
            case "clientPort":
                clientPort = Integer.parseInt(text);
                break;
            case "timeout":
                clientTimeout = Integer.parseInt(text);
                break;

        }

        buffer.setLength(0);
    }

    public Config getConfig() {
        if (serverHost == null || serverPort == null)
            throw new IllegalStateException("Config server incompleta");

        if (dbUrl == null || dbUser == null)
            throw new IllegalStateException("Config database incompleta");

        return new Config(
                serverHost, serverPort,
                dbUrl, dbUser, dbPassword,
                clientHost, clientPort,clientTimeout
        );
    }
}