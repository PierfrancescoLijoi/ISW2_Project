package org.isw2_project.commonfunctions;

import org.isw2_project.exception.JsonReadException;
import org.isw2_project.exception.SpecialRunException;
import org.json.JSONObject;


import java.io.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class JsonOperations {
    private JsonOperations(){}
    private static String readFile(Reader reader) throws IOException {
        StringBuilder strings= new StringBuilder();
        int asciiValue;
        while ((asciiValue = reader.read()) != -1) {
            strings.append((char) asciiValue);
        }
        return strings.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws JsonReadException {
    InputStream inputStream; //contiente tutto quello mostrato dal url
    try {
        inputStream =  new URI(url).toURL().openStream();
    } catch (IOException | URISyntaxException e) {
        throw new JsonReadException(e);
    }
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    String jsonText; //leggi tutto il contenuto in bufferedread che contiene tutto lo strewam
    try {
        jsonText = readFile(bufferedReader);
    } catch (IOException e) {
        throw new SpecialRunException(e);
    }
    return new JSONObject(jsonText);
}
}
