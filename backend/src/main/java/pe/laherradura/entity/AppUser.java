package pe.laherradura.entity;

import jakarta.persistence.*;
import pe.laherradura.enums.Role;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 150)
    private String fullName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Role role = Role.OPERATOR;
    @Column(nullable = false)
    private boolean active = true;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getUsername(){return username;} public void setUsername(String username){this.username=username;}
    public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
    public String getFullName(){return fullName;} public void setFullName(String fullName){this.fullName=fullName;}
    public Role getRole(){return role;} public void setRole(Role role){this.role=role;}
    public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
}
