package com.codes.bookshare.email;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    ACTIVATE_ACCOUNT("activate_account");

    private final String template;

    EmailTemplate(String template) {
        this.template = template;
    }
}
