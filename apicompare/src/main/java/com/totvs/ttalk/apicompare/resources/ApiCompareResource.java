package com.totvs.ttalk.apicompare.resources;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qdesrame.openapi.diff.OpenApiCompare;
import com.qdesrame.openapi.diff.model.ChangedOpenApi;
import com.qdesrame.openapi.diff.output.ConsoleRender;
import com.qdesrame.openapi.diff.output.JSONRender;

@RestController
@RequestMapping("/totvseai/openapicomparator/v1")
public class ApiCompareResource {

	@RequestMapping(path = "/console", method = RequestMethod.POST, produces = { "text/plain" })
	public String postConsole(@RequestBody String body) throws UnsupportedEncodingException, JSONException {
		return console(body);
	}

	@RequestMapping(path = "/json", method = RequestMethod.POST, produces = { "application/JSON" })
	public String postJSON(@RequestParam(value = "hasConsole", defaultValue = "true") Boolean hasConsole,
			@RequestBody String body) throws UnsupportedEncodingException, JSONException {
		return json(hasConsole, body);
	}

	@RequestMapping(path = "/console", method = RequestMethod.GET, produces = { "text/plain" })
	public String console(@RequestBody String body) throws UnsupportedEncodingException, JSONException {
		JSONObject jsonBody = new JSONObject(body);
		ChangedOpenApi result = compare(jsonBody.get("olderVersion").toString(),
				jsonBody.get("newerVersion").toString());

		ConsoleRender consoleRender = new ConsoleRender();
		String meuTexto = consoleRender.render(result);
		return meuTexto;
	}

	@RequestMapping(path = "/json", method = RequestMethod.GET, produces = { "application/JSON" })
	public String json(@RequestParam(value = "hasConsole", defaultValue = "true") Boolean hasConsole,
			@RequestBody String body) throws UnsupportedEncodingException, JSONException {

		JSONObject jsonBody = new JSONObject(body);

		ChangedOpenApi result = compare(jsonBody.get("olderVersion").toString(),
				jsonBody.get("newerVersion").toString());

		JSONRender jsonRender = new JSONRender();

		String responsejson = jsonRender.render(result, hasConsole);
		return responsejson;
	}

	private ChangedOpenApi compare(String olderVersion, String newerVersion) throws UnsupportedEncodingException {

		// converting into UTF-8
		String oVersion = new String(olderVersion.getBytes("UTF-8"));
		String nVersion = new String(newerVersion.getBytes("UTF-8"));

		ChangedOpenApi result = OpenApiCompare.fromContents(oVersion, nVersion);
		return result;
	}

}
