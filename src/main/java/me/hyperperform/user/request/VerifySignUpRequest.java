package me.hyperperform.user.request;

import me.hyperperform.user.EmployeeRole;
import me.hyperperform.user.Position;



/**
 * hyperperform-system
 * Group: CodusMaximus
 * Date: 2016/09/09
 * Feature:
 */
public class VerifySignUpRequest
{
    private String userName;
    private String userSurname;
    private String userEmail;
    private String userPassword;
    private EmployeeRole role;
    private Position position;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserSurname()
    {
        return userSurname;
    }

    public void setUserSurname(String userSurname)
    {
        this.userSurname = userSurname;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserPassword()
    {
        return userPassword;
    }

    public void setUserPassword(String userPassword)
    {
        this.userPassword = userPassword;
    }

    public EmployeeRole getRole()
    {
        return role;
    }

    public void setRole(EmployeeRole role)
    {
        this.role = role;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }
}
