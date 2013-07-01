package org.openmrs.module.html5forms.xForm2Html5Transform;


import org.openmrs.module.html5forms.api.impl.EnketoResult;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Stack;

public class EnketoXslTransformer extends XForm2Html5Transformer {

    private Stack<File> transforms;
    private SAXTransformerFactory transformerFactory;


    public EnketoXslTransformer(TransformerFactory transformerFactory, Stack<File> transforms) {
        this.transforms = transforms;
        this.transformerFactory = (SAXTransformerFactory) transformerFactory;
        transformerFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);


    }

    public EnketoResult transform(String xformXml) throws IOException, TransformerException, ParserConfigurationException {
        if (transforms.isEmpty()) return new EnketoResult("");

        StringWriter writer = new StringWriter();
        Result streamResult = new StreamResult(writer);

        Result intermediateResult = streamResult;
        while (!transforms.isEmpty()) {
            Templates templates = transformerFactory.newTemplates(new StreamSource(transforms.pop()));
            TransformerHandler transformerHandler = transformerFactory.newTransformerHandler(templates);
            transformerHandler.setResult(intermediateResult);
            intermediateResult = new SAXResult(transformerHandler);
        }

        File inputFile = createTempFile(xformXml);
        Transformer transformer = transformerFactory.newTransformer();
        try {
            transformer.transform(new StreamSource(inputFile), intermediateResult);
        } finally {
            inputFile.delete();
        }
        return new EnketoResult(writer.getBuffer().toString());
    }
}
