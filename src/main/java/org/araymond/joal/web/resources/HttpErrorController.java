package org.araymond.joal.web.resources;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This will never be called, but it prevent Spring from throwing exception when looking for error mapper.
 * Instead, spring return a 404 with no content.
 *
 * Created by raymo on 25/07/2017.
 */
@RestController
public class HttpErrorController implements ErrorController {

    private static final String PATH = "/error";

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @RequestMapping(value = PATH)
    public String error() {
        return "";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
