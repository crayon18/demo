package com.example.demo.account;

import com.example.demo.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception{
        mockMvc.perform(get("/check-email-token")
                .param("token","sdfjslwefe")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception{
        Account account = Account.builder()
                .email("test@email.com")
                .password("12345678")
                .nickname("jamesEE")
                .build();
        Account save = accountRepository.save(account);
        save.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token",save.getEmailCheckToken())
                .param("email",save.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());

    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname","keesun")
                        .param("email","keesun@email.com")
                        .param("password","12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("keesun@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(),"12345678");

        assertTrue(accountRepository.existsByEmail("keesun@email.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}
