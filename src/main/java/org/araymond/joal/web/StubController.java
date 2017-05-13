package org.araymond.joal.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Created by raymo on 01/05/2017.
 */
@RestController
@RequestMapping("/stub")
public class StubController {

    @RequestMapping(method = RequestMethod.GET)
    public List<String> nop() {
        return Collections.singletonList("hello");
    }

}
