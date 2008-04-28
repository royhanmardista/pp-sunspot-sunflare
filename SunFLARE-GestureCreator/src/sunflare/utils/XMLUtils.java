//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package sunflare.utils;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;


public final class XMLUtils {

  private XMLUtils() throws InstantiationException {
    throw new InstantiationException("Don't construct utility objects!");
  }

  public static Document getDocumentRoot(String xmlFile) {
    // open up the XML file
    DocumentBuilderFactory factory = null;
    DocumentBuilder parser = null;
    Document document = null;
    InputSource inputSource = null;

    InputStream xmlInputStream = null;

    try {
      xmlInputStream = new File(xmlFile).toURL().openStream();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    inputSource = new InputSource(xmlInputStream);

    try {
      factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
      document = parser.parse(inputSource);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return document;
  }

  public static org.w3c.dom.Element getFirstElement(String name,
      org.w3c.dom.Element root) {
    NodeList list = root.getElementsByTagName(name);
    if (list != null) {
      return (org.w3c.dom.Element) list.item(0);
    } else
      return null;
  }

  public static String getSimpleElementText(org.w3c.dom.Element node) {
    if (node.getChildNodes().item(0) instanceof Text) {
      return node.getChildNodes().item(0).getNodeValue();
    } else
      return null;
  }

  public static String getElementText(String elemName, org.w3c.dom.Element root) {
    org.w3c.dom.Element elem = getFirstElement(elemName, root);
    if (elem != null) {
      return getSimpleElementText(elem);
    } else
      return null;
  }

}
