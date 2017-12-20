package com.marklogic.mgmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.mgmt.util.ObjectMapperFactory;
import com.marklogic.rest.util.Fragment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Utility class for parsing a JSON or XML payload and extracting values.
 */
public class PayloadParser {

    private ObjectMapper objectMapper;

    public JsonNode parseJson(String json) {
    	if (objectMapper == null) {
    		objectMapper = ObjectMapperFactory.getObjectMapper();
	    }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to parse JSON: %s", e.getMessage()), e);
        }
    }

    public String getPayloadName(String payload, String idFieldName) {
        return getPayloadFieldValue(payload, idFieldName);
    }

	public String getPayloadFieldValue(String payload, String fieldName) {
    	return getPayloadFieldValue(payload, fieldName, true);
	}

	public String getPayloadFieldValue(String payload, String fieldName, boolean throwErrorIfNotFound) {
        if (isJsonPayload(payload)) {
            JsonNode node = parseJson(payload);
            if (!node.has(fieldName)) {
            	if (throwErrorIfNotFound) {
		            throw new RuntimeException("Cannot get field value from JSON; field name: " + fieldName + "; JSON: "
			            + payload);
	            } else {
            		return null;
	            }
            }
            return node.get(fieldName).isTextual() ? node.get(fieldName).asText() : node.get(fieldName).toString();
        } else {
            Fragment f = new Fragment(payload);
            String xpath = String.format("/node()/*[local-name(.) = '%s']", fieldName);
            if (!f.elementExists(xpath)) {
            	if (throwErrorIfNotFound) {
		            throw new RuntimeException("Cannot get field value from XML at path: " + xpath + "; XML: " + payload);
	            } else {
            		return null;
	            }
            }
            return f.getElementValues(xpath).get(0);
        }
    }

    public boolean isJsonPayload(String payload) {
    	if (payload == null) {
    		return false;
	    }
        String s = payload.trim();
        return s.startsWith("{") || s.startsWith("[");
    }

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String includeProperties(String payload, String[] propertyNames) {
		if (isJsonPayload(payload)){
			JsonNode json = parseJson(payload);
			ObjectNode node = (ObjectNode) json;
			Iterator<String> it = json.fieldNames();
			while(it.hasNext()){
				String name = it.next();
				if(!Arrays.asList(propertyNames).contains(name)) {
					it.remove();
				}
			}
			payload = node.toString();
		} else {
			//assume XML
			Fragment frag = new Fragment(payload);
			Element doc = frag.getInternalDoc().getRootElement();
			for(Element child : doc.getChildren()) {
				if(!Arrays.asList(propertyNames).contains(child.getName())) {
					child.detach();
				}
			}
			payload = new XMLOutputter().outputString(doc);
		}
		return payload;
	}

	public String excludeProperties(String payload, String[] propertyNames) {
		if (isJsonPayload(payload)){
			JsonNode json = parseJson(payload);
			for(String propertyName : propertyNames) {
				if (json.has(propertyName)) {
					ObjectNode node = (ObjectNode) json;
					node.remove(propertyName);
				}
			}
			payload = json.toString();
		} else {
			//assume XML
			Fragment frag = new Fragment(payload);
			Element doc = frag.getInternalDoc().getRootElement();
			for(String propertyName : propertyNames) {
				if (frag.elementExists(propertyName)) {
					Element el = doc.getChild(propertyName);
					el.detach();
				}
			}
			payload = new XMLOutputter().outputString(doc);
		}
		return payload;
	}
}
