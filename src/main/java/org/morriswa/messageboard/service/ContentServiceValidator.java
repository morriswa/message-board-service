package org.morriswa.messageboard.service;

import org.morriswa.messageboard.validation.BasicBeanValidator;
import org.springframework.stereotype.Service;

@Service
public class ContentServiceValidator extends BasicBeanValidator {
    public ContentServiceValidator() {
        super();
    }
}
