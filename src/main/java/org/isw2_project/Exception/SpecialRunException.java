package org.isw2_project.Exception;

import java.io.IOException;

public class SpecialRunException extends RuntimeException {
    public SpecialRunException(IOException e) {
        super (e);
    }
}
