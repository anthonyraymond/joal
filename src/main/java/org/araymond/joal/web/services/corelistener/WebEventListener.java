package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.web.services.JoalMessageSendingTemplate;

/**
 * Created by raymo on 29/06/2017.
 */
public abstract class WebEventListener {

    protected final JoalMessageSendingTemplate messagingTemplate;

    public WebEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
}
