package it.universita.config;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class ConfigSAXParser {

    public static Config fromXmlFile(String resourceName) throws Exception {

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
        // 6) Avvio il parsing passando uno stream sul
        InputStream is = ConfigSAXParser.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalStateException("File di config " + resourceName + " non trovato nel classpath");
        }
        xmlReader.parse(new InputSource(is));
        // 7) Recupero i dati raccolti dall'handler
        Config config = handler.getConfig();
        if (config == null ) {
            throw new IllegalStateException("Config XML mancante di qualche elemento");
        }

        return config;
    }
}
