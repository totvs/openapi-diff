package com.qdesrame.openapi.diff.output;

import com.qdesrame.openapi.diff.model.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Schema;
import org.json.JSONArray;
import org.json.JSONObject;

//import javax.xml.validation.Schema;

public class JSONRender implements Render {

	@Override
	public String render(ChangedOpenApi diff) {
		return render(diff, false);
	}

	public String render(ChangedOpenApi diff, boolean hasConsoleRender) {

		JSONObject jsonRender = new JSONObject();
		if (diff.isUnchanged()) {
			jsonRender.put("hadChanges", false);
			jsonRender.put("isBackwardCompatible", true);
		} else {
			JSONArray newRender = new JSONArray();
			JSONArray missingRender = new JSONArray();
			JSONArray changedRender = new JSONArray();

			List<Endpoint> newEndpoints = diff.getNewEndpoints();
			newEndpoints.forEach(newEndpoint -> newRender.put(newEndpoint));

			List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
			missingEndpoints.forEach(missingEndpoint -> missingRender.put(missingEndpoint));

			List<ChangedOperation> changedOperations = diff.getChangedOperations();
			changedOperations.forEach(changedOperation -> {
				JSONObject operationRender = new JSONObject();
				operationRender.put("pathURL", changedOperation.getPathUrl());
				operationRender.put("httpMethod", changedOperation.getHttpMethod());
				operationRender.put("description", changedOperation.getDescription());
				operationRender.put("parameters", listParam(changedOperation.getParameters()));
				operationRender.put("request", listContent(changedOperation.getRequestBody()));
				operationRender.put("returnType", listResponse(changedOperation.getApiResponses()));
				operationRender.put("isBackwardCompatible", changedOperation.isCompatible());
				changedRender.put(operationRender);
			});

			jsonRender.put("hadChanges", true);
			jsonRender.put("added", newRender);
			jsonRender.put("deleted", missingRender);
			jsonRender.put("changed", changedRender);
			jsonRender.put("isBackwardCompatible", diff.isCompatible());

			if (hasConsoleRender) {
				ConsoleRender consoleRender = new ConsoleRender();
				jsonRender.put("consoleRender", consoleRender.render(diff));
			}

		}

		return jsonRender.toString();
	}

	private JSONObject listParam(ChangedParameters changedParameters) {
		if (changedParameters == null) {
			return null;
		}

		List<Parameter> addParameters = changedParameters.getIncreased();
		List<Parameter> deleteParameters = changedParameters.getMissing();
		List<ChangedParameter> changed = changedParameters.getChanged();

		JSONArray addParametersJSON = new JSONArray();
		JSONArray deleteParametersJSON = new JSONArray();
		JSONArray changedParametersJSON = new JSONArray();

		JSONObject jsonReturn = new JSONObject();

		for (Parameter param : addParameters) {
			addParametersJSON.put(itemParam(param));
		}
		jsonReturn.put("added", addParametersJSON);

		for (Parameter param : deleteParameters) {
			deleteParametersJSON.put(itemParam(param));
		}
		jsonReturn.put("deleted", deleteParametersJSON);

		for (ChangedParameter param : changed) {
			changedParametersJSON.put(listChangedParam(param));
		}
		jsonReturn.put("changed", changedParametersJSON);

		jsonReturn.put("isBackwardCompatible", changedParameters.isCompatible());

		return jsonReturn;
	}

	private JSONObject itemParam(Parameter param) {
		JSONObject JSONReturn = new JSONObject();
		JSONReturn.put("name", param.getName());
		JSONReturn.put("in", param.getIn());
		return JSONReturn;
	}

	private JSONObject listChangedParam(ChangedParameter changeParam) {
		JSONObject jsonReturn = new JSONObject();

		if (changeParam.isDeprecated()) {
			jsonReturn.put("deprecated", itemParam(changeParam.getNewParameter()));
		} else {
			jsonReturn.put("changed", itemParam(changeParam.getNewParameter()));
		}
		return jsonReturn;
	}

	private JSONObject listContent(ChangedRequestBody changedRequestBody) {
		// Protection to not access a property of a null
		if (changedRequestBody == null) {
			return null;
		}
		return listContent(changedRequestBody.getContent());
	}

	private JSONObject listContent(ChangedContent changedContent) {

		JSONObject jsonReturn = new JSONObject();

		if (changedContent == null) {
			return jsonReturn;
		}
		for (String propName : changedContent.getIncreased().keySet()) {
			jsonReturn.put("added", propName);
		}
		for (String propName : changedContent.getMissing().keySet()) {
			jsonReturn.put("deleted", propName);
		}
		for (String propName : changedContent.getChanged().keySet()) {
			jsonReturn.put("changed", itemContent(propName, changedContent.getChanged().get(propName)));
		}

		jsonReturn.put("isBackwardCompatible", changedContent.isCompatible());
		return jsonReturn;
	}

	private JSONObject itemContent(String contentType, ChangedMediaType changedMediaType) {
		JSONObject JSONReturn = new JSONObject();

		JSONReturn.put("contentType", contentType);
		JSONReturn.put("IsBackwardCompatible", changedMediaType.isCompatible());
		JSONReturn.put("schema", listSchema(changedMediaType.getSchema()));

		return JSONReturn;
	}

	private JSONObject listResponse(ChangedApiResponse changedApiResponse) {
		if (changedApiResponse == null) {
			return null;
		}
		Map<String, ApiResponse> addResponses = changedApiResponse.getIncreased();
		Map<String, ApiResponse> deleteResponses = changedApiResponse.getMissing();
		Map<String, ChangedResponse> changedResponses = changedApiResponse.getChanged();

		JSONArray addResponsesJSON = new JSONArray();
		JSONArray deleteResponsesJSON = new JSONArray();
		JSONArray changedResponsesJSON = new JSONArray();

		JSONObject jsonReturn = new JSONObject();

		for (String propName : addResponses.keySet()) {
			addResponsesJSON.put(propName);
		}
		for (String propName : deleteResponses.keySet()) {
			deleteResponsesJSON.put(propName);
		}
		for (String propName : changedResponses.keySet()) {
			changedResponsesJSON.put(itemChangedResponse(propName, changedResponses.get(propName)));
		}

		jsonReturn.put("added", addResponsesJSON);
		jsonReturn.put("deleted", deleteResponsesJSON);
		jsonReturn.put("changed", changedResponsesJSON);
		jsonReturn.put("isBackwardCompatible", changedApiResponse.isCompatible());

		return jsonReturn;
	}

	private JSONObject itemChangedResponse(String contentType, ChangedResponse response) {
		JSONObject jsonReturn = new JSONObject();

		jsonReturn.put("contentType", contentType);
		jsonReturn.put("mediaType", listContent(response.getContent()));
		return jsonReturn;
	}

	private JSONObject listSchema(ChangedSchema changedSchema) {

		if (changedSchema == null)
			return null;

		Map<String, Schema> addProperties = changedSchema.getIncreasedProperties();
		Map<String, Schema> deleteProperties = changedSchema.getMissingProperties();
		Map<String, ChangedSchema> changedProperties = changedSchema.getChangedProperties();

		JSONArray addPropertiesJSON = new JSONArray();
		JSONArray deletePropertiesJSON = new JSONArray();
		JSONArray changedPropertiesJSON = new JSONArray();

		for (String propName : addProperties.keySet()) {
			addPropertiesJSON.put(propName);
		}
		for (String propName : deleteProperties.keySet()) {
			deletePropertiesJSON.put(propName);
		}
		for (String propName : changedProperties.keySet()) {
			changedPropertiesJSON.put(itemChangedProperty(propName, changedProperties.get(propName)));
		}

		JSONObject jsonReturn = new JSONObject();

		jsonReturn.put("added", addPropertiesJSON);
		jsonReturn.put("deleted", deletePropertiesJSON);
		jsonReturn.put("changed", changedPropertiesJSON);
		jsonReturn.put("isBackwardCompatible", changedSchema.isCompatible());

		return jsonReturn;
	}

	private JSONObject itemChangedProperty(String propertyName, ChangedSchema changedProperty) {

		JSONObject jsonReturn = new JSONObject();

		jsonReturn.put("property", propertyName);
		jsonReturn.put("type", changedProperty.getType());
		if (changedProperty.getDescription() != null)
			jsonReturn.put("description", changedProperty.getDescription().getRight());
		if (changedProperty.getRequired() != null)
			jsonReturn.put("isChangeRequired", changedProperty.getRequired().isItemsChanged().isUnchanged());
		if (changedProperty.getEnumeration() != null)
			jsonReturn.put("enumeration", changedProperty.getEnumeration().getNewValue());

		jsonReturn.put("isBackwardCompatible", changedProperty.isCompatible());

		if (changedProperty.getType() == "array") {
			jsonReturn.put("properties", listSchema(changedProperty.getItems()));
		} else {
			if (!(changedProperty.getIncreasedProperties().isEmpty() && changedProperty.getMissingProperties().isEmpty()
					&& changedProperty.getChangedProperties().isEmpty()))
				jsonReturn.put("properties", listSchema(changedProperty));
		}
		return jsonReturn;
	}
}
