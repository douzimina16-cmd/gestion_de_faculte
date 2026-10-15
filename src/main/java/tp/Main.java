package tp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * TP : gestion de faculté.
 * Fonctionnalité 1 : validation XML / XSD (SchemaFactory + Validator)
 * Fonctionnalité 2 : interrogation XPath (XPathFactory + XPathExpression)
 */
public final class Main {

    static final String XML_FILE = "/faculte.xml";
    static final String XSD_FILE = "/faculte.xsd";

    // ---------- Fonctionnalité 1 : validation ----------
    public static boolean valider(String xmlRes, String xsdRes) {
        try (InputStream xml = Main.class.getResourceAsStream(xmlRes);
             InputStream xsd = Main.class.getResourceAsStream(xsdRes)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            System.out.println("Validation : le document " + xmlRes + " est VALIDE.");
            return true;
        } catch (SAXException e) {
            System.out.println("Validation : document INVALIDE -> " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    // ---------- Fonctionnalité 2 : XPath ----------
    static Document charger(String xmlRes) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try (InputStream in = Main.class.getResourceAsStream(xmlRes)) {
            return dbf.newDocumentBuilder().parse(in);
        }
    }

    public static List<String> requete(Document doc, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expression);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            res.add(n.getTextContent().trim());
        }
        return res;
    }

    /** Pour les expressions qui renvoient un nombre (count, sum, avg...). */
    public static double nombre(Document doc, String expression) throws Exception {
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile(expression);
        return (Double) expr.evaluate(doc, XPathConstants.NUMBER);
    }

    static void afficher(Document doc, String titre, String expression) throws Exception {
        System.out.println("\n" + titre);
        System.out.println("  XPath : " + expression);
        for (String s : requete(doc, expression)) {
            System.out.println("   - " + s);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Validation XML / XSD ===");
        if (!valider(XML_FILE, XSD_FILE)) {
            return; // on n'interroge pas un document invalide
        }

        System.out.println("\n=== 2. Interrogation XPath ===");
        Document doc = charger(XML_FILE);

        afficher(doc, "Noms de tous les cours",
                "//cour/nom");
        afficher(doc, "Emails de tous les étudiants",
                "//etudiant/email");
        afficher(doc, "Enseignants ayant le grade Professeur",
                "//enseignant[grade='professeur']/nom");
        afficher(doc, "Cours dont le coefficient est supérieur ou égal à 3",
                "//cour[coefficient >= 3]/nom");
        afficher(doc, "Notes supérieures ou égales à 15",
                "//note[. >= 15]");
        afficher(doc, "Nom du cours dont l identifiant est c1",
                "//cour[@id='c1']/nom");
        String exprCount = "count(//etudiant)";
        System.out.println("\nNombre d'étudiants");
        System.out.println("  XPath : " + exprCount);
        System.out.println("   - " + (int) nombre(doc, exprCount));
    }
}
