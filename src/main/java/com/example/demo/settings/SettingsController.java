package com.example.demo.settings;

import com.example.demo.Zone.ZoneForm;
import com.example.demo.Zone.ZoneRepository;
import com.example.demo.account.AccountService;
import com.example.demo.account.CurrentUser;
import com.example.demo.domain.Account;
import com.example.demo.domain.Tag;
import com.example.demo.domain.Zone;
import com.example.demo.settings.form.*;
import com.example.demo.settings.validator.NicknameValidator;
import com.example.demo.settings.validator.PasswordFormValidator;
import com.example.demo.tag.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors, Model model,
                                RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message","프로필을 수정했습니다.");
        return "redirect:" + "/settings/profile";
    }


    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message","패스워드를 변경했습니다.");
        return "redirect:" + "/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + "/settings/notifications";
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);

        if (tag == null){
            tag = tagRepository.save(Tag.builder()
                    .title(tagForm.getTagTitle())
                    .build());
        }

        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);

        if (tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/zones")
    public String updateZoneForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones",zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allzones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allzones));

        return "settings/zones";
    }

    @PostMapping("/settings/zones/add")
    public ResponseEntity addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());

        if (zone == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account,zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/zones/remove")
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account,zone);
        return ResponseEntity.ok().build();
    }




    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account,Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                    Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message","닉네임을 수정했습니다.");
        return "redirect:/" + "settings/account";
    }
}
