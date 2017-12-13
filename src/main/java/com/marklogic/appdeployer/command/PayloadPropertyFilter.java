package com.marklogic.appdeployer.command;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.mgmt.PayloadParser;
import com.marklogic.rest.util.Fragment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class PayloadPropertyFilter {

	private PayloadParser payloadParser = new PayloadParser();

	public String includeProperty(String payload, String propertyName, String propertyValue) {
		if (payloadParser.isJsonPayload(payload)){
			JsonNode json = payloadParser.parseJson(payload);
			ObjectNode node = (ObjectNode) json;
			node.put(propertyName, propertyValue);
			payload = node.toString();
		} else {
			//assume XML
			Fragment frag = new Fragment(payload);
			Element doc = frag.getInternalDoc().getRootElement();
			Element el = new Element(propertyName);
			el.setText(propertyValue);
			doc.addContent(el);
			payload = new XMLOutputter().outputString(doc);
		}
		return payload;
	}

	public String excludeProperty(String payload, String propertyName) {
		if (payloadParser.isJsonPayload(payload)){
			JsonNode json = payloadParser.parseJson(payload);
			if (json.has(propertyName)){
				ObjectNode node = (ObjectNode) json;
				node.remove(propertyName);
				payload = node.toString();
			}
		} else {
			//assume XML
			Fragment frag = new Fragment(payload);
			if (frag.elementExists(propertyName)) {
				Element doc = frag.getInternalDoc().getRootElement();
				Element el = doc.getChild(propertyName);
				el.detach();
				payload = new XMLOutputter().outputString(doc);
			}
		}
		return payload;
	}

}
