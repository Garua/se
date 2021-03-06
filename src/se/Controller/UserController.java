/**
 * @author: created by wwbweibo
 * @version: 1.0
 * @date: 2019/4/12
 */
package se.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import se.listener.StartupListener;
import se.model.Mapper.StudentMapper;
import se.model.Mapper.UserMapper;
import se.model.User;
import se.utils.DbUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class UserController {

    /**
     * post back page for login form
     *
     * @param username username
     * @param password password
     * @return FormMain if success, Login if false
     */
    @RequestMapping(value = "/User/Login", method = RequestMethod.POST)
    public String Login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        User u = new User();
        u.setUserId(username);
        u.setPwd(password);
        boolean success = u.Login();

        if (success) {
            session.setAttribute("user", u);
            return "redirect:/FormMain";
        } else {
            return "redirect:/Login";
        }
    }

    /**
     * check if userid and email addr matched
     *
     * @param userId   userid
     * @param mailAddr email address
     * @return JSON string
     */
    @RequestMapping(value = "/User/MailCheck", method = RequestMethod.GET)
    public String MailCheck(@RequestParam String userId, @RequestParam String mailAddr) {
        //todo check userId is match mailAddr
        return null;
    }

    /**
     * a page that allow user to reset pwd
     *
     * @param UserId   userid
     * @param response reponse obj
     * @return page"resetPassword"
     */
    @RequestMapping(value = "/User/ResetPassword", method = RequestMethod.GET)
    public String ResetPassword(@RequestParam String UserId, HttpServletResponse response) {
        try {
            response.getWriter().write("<script>alert('" + UserId + "')</script>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "/User/Checkin")
    public Object Checkin(HttpSession session) {
        // get current user
        User user = (User) session.getAttribute("user");
        CheckinStatus status = new CheckinStatus();
        Date now = new Date();
        if (now.getHours() == StartupListener.config.getCheckInTime().getHours()) {
            if (user == null) {
                status.setResult(false);
                status.setMessage("用户未登录");

            } else if (user.getRole() != 2) {
                status.setUserid(user.getUserId());
                status.setMessage("签到只允许学生进行");
                status.setResult(false);
                status.setCheckin("checkin");
            } else if (user.getRole() == 2) {
                DbUtils<StudentMapper> dbUtils = new DbUtils<>(StudentMapper.class);
                boolean isTodayCheckedIn = dbUtils.mapper.IsTodayCheckedIn(user.getUserId());
                if (!isTodayCheckedIn) {
                    dbUtils.mapper.CheckIn(user.getUserId());
                    dbUtils.session.commit();
                    status.setUserid(user.getUserId());
                    status.setResult(true);
                    status.setMessage("签到成功");
                    status.setCheckin("checkin");
                } else {
                    status.setUserid(user.getUserId());
                    status.setResult(false);
                    status.setMessage("签到失败,今日已经签到！");
                    status.setCheckin("checkin");
                }
            }
        } else {
            status.setResult(false);
            status.setMessage("现在还不是签到时间");
        }
        return status;
    }

    @ResponseBody
    @RequestMapping("/User/Checkout")
    public Object CheckOut(HttpSession session) {
        User user = (User) session.getAttribute("user");
        CheckinStatus status = new CheckinStatus();
        Date now = new Date();
        if (now.getHours() == StartupListener.config.getCheckInTime().getHours()) {
            if (user == null) {
                status.setResult(false);
                status.setMessage("用户未登录");

            } else if (user.getRole() != 2) {
                status.setUserid(user.getUserId());
                status.setMessage("签到只允许学生进行");
                status.setResult(false);
                status.setCheckin("checkout");
            } else if (user.getRole() == 2) {
                DbUtils<StudentMapper> dbUtils = new DbUtils<>(StudentMapper.class);
                boolean isTodeyCheckedIn = dbUtils.mapper.IsTodayCheckedIn(user.getUserId());
                if (isTodeyCheckedIn) {
                    dbUtils.mapper.CheckOut(user.getUserId());
                    dbUtils.session.commit();
                    status.setUserid(user.getUserId());
                    status.setResult(true);
                    status.setMessage("签退成功");
                    status.setCheckin("checkout");
                } else {
                    status.setUserid(user.getUserId());
                    status.setResult(false);
                    status.setMessage("签退失败,今日未签到！");
                    status.setCheckin("checkOut");
                }
            }
        } else {
            status.setMessage("还不是签退时间");
            status.setResult(false);
        }
        return status;
    }

    @ResponseBody
    @RequestMapping("/User/isTodayCheckedIn")
    public HashMap<String, Boolean> IsTodayCheckedIn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user.getRole() == 2) {
            DbUtils<StudentMapper> dbUtils = new DbUtils<>(StudentMapper.class);
            boolean isTodayCheckedIn = dbUtils.mapper.IsTodayCheckedIn(user.getUserId());
            HashMap<String, Boolean> ret = new HashMap<>();
            ret.put("CheckedIn", (Boolean) isTodayCheckedIn);
            return ret;
        }
        return null;
    }


    @ResponseBody
    @RequestMapping("/User/AllTeacher")
    public List<User> AllTeacher() {
        DbUtils<UserMapper> dbUtils = new DbUtils<>(UserMapper.class);
        return dbUtils.mapper.QueryAllTeacher();
    }

    class CheckinStatus {
        String userid;
        String Checkin;
        Boolean result;
        String message;

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getCheckin() {
            return Checkin;
        }

        public void setCheckin(String checkin) {
            Checkin = checkin;
        }

        public Boolean getResult() {
            return result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
