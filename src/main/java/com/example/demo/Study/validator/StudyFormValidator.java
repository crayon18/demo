package com.example.demo.Study.validator;

import com.example.demo.Study.StudyRepositroy;
import com.example.demo.Study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepositroy studyRepositroy;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if (studyRepositroy.existsByPath(studyForm.getPath())){
            errors.rejectValue("path","wrong.path","스터디 경로를 사용할 수 없습니다");
        }
    }
}
