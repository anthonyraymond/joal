package org.araymond.joal;

import org.araymond.joal.springtestconf.MockedSeedManagerBean;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
public class DefaultWebContextTest {
    public static final String UI_PATH_PREFIX = "ui-prefix";
    public static final String UI_SECRET_TOKEN = "secret-token";


}
