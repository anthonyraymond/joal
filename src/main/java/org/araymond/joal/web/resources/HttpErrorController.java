package org.araymond.joal.web.resources;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This will never be called, but it prevent Spring from throwing exception when looking for error mapper.
 * Instead, spring return a 404 with no content when trying to access the server with HTTP requests.
 *
 * Created by raymo on 25/07/2017.
 */
@ConditionalOnWebUi
@RestController //Use @RestController over @Controller because it allow to return String. @Controller interpret Strings as view to resolve.
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
