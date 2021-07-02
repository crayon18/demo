package com.example.demo.Study;

import com.example.demo.Study.form.StudyDescriptionForm;
import com.example.demo.domain.Zone;
import com.example.demo.Zone.ZoneForm;
import com.example.demo.Zone.ZoneRepository;
import com.example.demo.account.CurrentUser;
import com.example.demo.domain.Account;
import com.example.demo.domain.Study;
import com.example.demo.domain.Tag;
import com.example.demo.settings.form.TagForm;
import com.example.demo.tag.TagRepository;
import com.example.demo.tag.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/study/{path}/settings/description")
    public String viewStudySetting(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/study/{path}/settings/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" +getPath(path) + "/settings/description";
    }

    private String getPath(String path){
        return URLEncoder.encode(path,StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}/settings/banner")
    public String studyImageForm(@CurrentUser Account account,@PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/study/{path}/settings/banner")
    public String studyImageSubmit(@CurrentUser Account account , @PathVariable String path, String image, RedirectAttributes attributes){

        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study,image);
        attributes.addFlashAttribute("message","스터디 이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/study/{path}/settings/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/study/{path}/settings/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @GetMapping("/study/{path}/settings/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tag",study.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitle = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whiteList",objectMapper.writeValueAsString(allTagTitle));

        return "study/settings/tags";
    }

    @PostMapping("/study/{path}/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/study/{path}/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study,path);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study/{path}/settings/zones")
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones", study.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @PostMapping("/study/{path}/settings/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/study/{path}/settings/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study/{path}/settings/study")
    public String studySettingForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(study);
        model.addAttribute(account);
        return "study/settings/study";
    }

    @PostMapping("/study/{path}/settings/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message","스터디를 종료했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","인원 모집을 시작합니다");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","1시간안에 인원 모집 설정을 변경할수 없습니다");
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message","인원 모집을 종료합니다");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, @RequestParam String newPath,
                                  Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute("studyPathError", "해당스터디 경로는 사용할 수 없습니다.");
            return "study/setting/study";
        }

        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message","스터디 경로를 수정했습니다");
        return "redirect:/study/" + getPath(newPath) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, @RequestParam String newTitle,
                                  Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute("studyPathError", "스터디 이름을 다시 입력하세요.");
            return "study/setting/study";
        }

        studyService.updateStudyTitle(study, newTitle);
        attributes.addFlashAttribute("message","스터디 이름을 수정했습니다");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/{path}/settings/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.removeStudy(study);
        return "redirect:/";
    }

}
