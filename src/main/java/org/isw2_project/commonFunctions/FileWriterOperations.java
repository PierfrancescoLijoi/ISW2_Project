package org.isw2_project.commonFunctions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class FileWriterOperations {


    public static void flushAndCloseFW(FileWriter fileWriter, Logger logger, String nameOfThisClass) {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Error in " + nameOfThisClass + " while flushing/closing fileWriter !!!");
        }
    }
}
