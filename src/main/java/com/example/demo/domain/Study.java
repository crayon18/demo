package com.example.demo.domain;

import com.example.demo.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor
@NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean userBanner;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);

    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

    public void publish(){
        if (!this.closed && !this.published){
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (this.published && !this.closed){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료할 수 없는 상태입니다. 스터디를 공개하지않았거나 종료했습니다.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusSeconds(1));
    }

    public void startRecruit() {
        if (canUpdateRecruiting()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 시작할수 없습니다");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 중지할 수 없습니다");
        }
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public String getEncodedPath(){
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }
}
