package org.araymond.joal.web.services.corelistener;

import lombok.RequiredArgsConstructor;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;

/**
 * Created by raymo on 29/06/2017.
 */
@RequiredArgsConstructor
public abstract class WebEventListener {

    protected final JoalMessageSendingTemplate messagingTemplate;
}
