package com.example.demo.event.validator;

import com.example.demo.domain.Event;
import com.example.demo.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if (eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now())){
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요");
        }

        if (eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime()
                .isBefore(eventForm.getEndEnrollmentDateTime())){
            errors.rejectValue("endDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요");
        }

        if (eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime())){
            errors.rejectValue("endDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요");
        }
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()){
            errors.rejectValue("limitOfEnrollments","wrong.value","확인된 참가 신청보다 모집 인원 수가 많아야 합니다");
        }
    }
}
