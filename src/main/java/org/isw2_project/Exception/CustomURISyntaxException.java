package org.isw2_project.Exception;

import java.net.URISyntaxException;

public class CustomURISyntaxException extends RuntimeException {
    public CustomURISyntaxException(URISyntaxException e) {
        super(e);
    }
}
