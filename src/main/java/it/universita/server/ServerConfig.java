package it.universita.server;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;

public class ServerConfig {
    private final int porta;
    private final String host;

    public ServerConfig( String host, int porta) {
        this.porta = porta;
        this.host = host;
    }

    public int getPorta() {
        return porta;
    }

    public String getHost() {
        return host;
    }

    public static ServerConfig fromXmlFile(String path) throws Exception {

        // 1) Creo la factory serve per creare parser SAX
        SAXParserFactory spf = SAXParserFactory.newInstance();
        // 2) Dalla factory ottengo un SAXParser
        SAXParser saxParser = spf.newSAXParser();
        // 3) Dal SAXParser ottengo l'XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();
        // 4) Creo il mio handler personalizzato che va a leggere il file config
        ConfigHandler handler = new ConfigHandler();
        // 5) Registro l'handler come ContentHandler dell'XMLReader
        xmlReader.setContentHandler(handler);
        // 6) Avvio il parsing passando uno stream sul file
        try (FileInputStream fis = new FileInputStream(path)) {
            InputSource inputSource = new InputSource(fis);
            xmlReader.parse(inputSource);
        }
        // 7) Recupero i dati raccolti dall'handler
        String host = handler.getHost();
        Integer port = handler.getPort();

        if (host == null || port == null) {
            throw new IllegalStateException("Config XML mancante di <host> o <port>");
        }

        return new ServerConfig(host, port);
    }

    //Handler SAX interno (inner class) che estende DefaultHandler
    private static class ConfigHandler extends DefaultHandler {

        private final StringBuilder buffer = new StringBuilder();
        private String host;
        private Integer port;

        @Override
        // Questo metodo viene invocato quando SAX incontra un tag di apertura.
        // Qui ci prepariamo a leggere il contenuto del nuovo elemento svuotando il buffer.
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            // nuovo tag → svuoto il buffer del testo
            buffer.setLength(0);
        }

        @Override
        // Questo metodo viene invocato quando SAX trova del testo dentro un elemento.
        // Può essere chiamato più volte per lo stesso tag, quindi accumuliamo il testo nel buffer.
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            // accumulo il testo che trovo (può arrivare a pezzi)
            buffer.append(ch, start, length);
        }

        @Override
        // Questo metodo viene invocato alla chiusura di un tag.
        // A questo punto il buffer contiene tutto il testo di quell'elemento:
        // in base al nome del tag (qName) decidiamo in quale variabile salvarlo (host o port).
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            String content = buffer.toString().trim();

            if ("host".equals(qName)) {
                host = content;
            } else if ("port".equals(qName)) {
                try {
                    port = Integer.parseInt(content);
                } catch (NumberFormatException e) {
                    throw new SAXException("Valore non valido per <port>: " + content, e);
                }
            }
            // finito questo elemento → reset del buffer
            buffer.setLength(0);
        }

        public String getHost() { return host; }
        public Integer getPort() { return port; }
    }
}
