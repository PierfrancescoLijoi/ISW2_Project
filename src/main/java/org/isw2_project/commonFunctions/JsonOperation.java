package org.isw2_project.commonFunctions;

import org.json.JSONObject;


import java.io.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class JsonOperation {
    private JsonOperation(){}
    private static String readFile(Reader reader) throws IOException {
        StringBuilder strings= new StringBuilder();
        int asciiValue;
        while ((asciiValue = reader.read()) != -1) {
            strings.append((char) asciiValue);
        }
        return strings.toString();
    }

    public static JSONObject readJsonFromUrl(String url) {
        InputStream inputStream= null; //contiente tutto quello mostrato dal url
        try {
            inputStream =  new URI(url).toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String jsonText = null; //leggi tutto il contenuto in bufferedread che contiene tutto lo strewam
        try {
            jsonText = readFile(bufferedReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject(jsonText);
    }
}
