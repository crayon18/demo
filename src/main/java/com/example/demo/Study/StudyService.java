package com.example.demo.Study;

import com.example.demo.Study.form.StudyDescriptionForm;
import com.example.demo.domain.Zone;
import com.example.demo.domain.Account;
import com.example.demo.domain.Study;
import com.example.demo.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.Study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepositroy studyRepositroy;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepositroy.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path){
        Study study = getStudy(path);
        checkIfManager(account,study);
        return study;
    }

    public Study getStudy(String path){
        Study study = studyRepositroy.findByPath(path);
        if (study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm){
        modelMapper.map(studyDescriptionForm, study);
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study){
        study.setUserBanner(true);
    }

    public void disableStudyBanner(Study study){
        study.setUserBanner(false);
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepositroy.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void removeTag(Study study, String path) {
        study.getTags().remove(path);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepositroy.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study){
        study.close();
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepositroy.findStudyWithMangersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account,study);
        return study;
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepositroy.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle){
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle){
        study.setTitle(newTitle);
    }

    public void removeStudy(Study study) {
        if (study.isRemovable()){
            studyRepositroy.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다");
        }
    }

    public void addMember(Study study, Account account) {
        study.getMembers().add(account);
    }

    public void removeMember(Study study, Account account) {
        study.getMembers().remove(account);
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepositroy.findStudyOnlyByPath(path);
        checkIfExistingStudy(path,study);
        return study;
    }
}
