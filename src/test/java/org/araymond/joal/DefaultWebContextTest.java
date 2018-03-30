package org.araymond.joal;

import org.araymond.joal.springtestconf.MockedSeedManagerBean;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-environment=true",
                "joal.ui.path.prefix=" + DefaultWebContextTest.UI_PATH_PREFIX,
                "joal.ui.secret-token=" + DefaultWebContextTest.UI_SECRET_TOKEN
        }
)
@Import(MockedSeedManagerBean.class)
public abstract class DefaultWebContextTest {
    public static final String UI_PATH_PREFIX = "ui-prefix";
    public static final String UI_SECRET_TOKEN = "secret-token";

    @Inject
    protected WebApplicationContext context;
    protected MockMvc mvc;

    @Before
    public void initTests() {
        MockitoAnnotations.initMocks(this);
        this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

}
