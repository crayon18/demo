package com.example.demo.settings.form;

import com.example.demo.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Notifications {

    private boolean studyCreateByEmail;

    private boolean studyCreateByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdateByWeb;

}

