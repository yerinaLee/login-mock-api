package com.study.auth.service;

import com.study.auth.Repository.UserRepository;
import com.study.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> PortalLoginJSON(HttpServletRequest request){

        Map<String, Object> paramMap = new HashMap<>();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        String msg = "success";
        Integer returnCode = 1;

        if(userOpt.isEmpty()){
            msg = "invalid login request";
            returnCode = 50000;

            paramMap.put("msg", msg);
            paramMap.put("returnCode", returnCode);
            return paramMap;
        }

        User user = userOpt.get();

        // 비밀번호 매칭
        if(returnCode == 1){
            if(!passwordEncoder.matches(password, user.getPassword())){
                msg = "invalid login request";
                returnCode = 50000;
            }
        }

        String newAuthKey = UUID.randomUUID().toString(); // 로그인 할때마다 authkey는 새로 신설

        // authKey를 User 테이블에 추가함
        if(returnCode == 1){
            user.setAuthKey(newAuthKey);
            userRepository.save(user);
        }

//        paramMap.put("authKey", newAuthKey);
        paramMap.put("userNo", user.getId());
        paramMap.put("userRole", user.getRoles());
        paramMap.put("msg", msg);
        paramMap.put("returnCode", returnCode);

        return paramMap;
    }

    public Map<String, Object> updateSessionInformation(HttpServletRequest request, Long userNo){
        return getAuthKeyBuUserNoParamJSON(request, userNo);
    }

    // authkey + tokkenkey 반환
    public Map<String, Object> getAuthKeyBuUserNoParamJSON(HttpServletRequest request, Long userNo){
        Integer returnCode = 1;
        String msg = "success";

        User user = null;
        String authKey = "";
        String tokken = "";

        Optional<User> userOpt = userRepository.findById(userNo);

        if(!userOpt.isEmpty()){
            user = userOpt.get();

            authKey = user.getAuthKey();

            //유저의 토큰키 컬럼값이 없는 경우에만 토큰 키 생성 및 DB 저장
            if(user.getTokkenkey() == null || user.getTokkenkey().isEmpty()){
                tokken = (authKey != null && !authKey.isEmpty()) ?
                        System.currentTimeMillis() + "-" + user.getId() + "-" + authKey :
                        System.currentTimeMillis() + "-" + user.getId() + "-" + UUID.randomUUID().toString();

                userRepository.updateTokkenKey(userNo, tokken);
            } else {
                tokken = user.getTokkenkey();
            }

        } else {
            msg = "invalid user";
            returnCode = 50002;
        }

        String[] tokkenArray = tokken.split("-");

        Map<String, Object> result = new HashMap<>();

        if(returnCode == 1){
            result.put("authKey", !authKey.isEmpty() ? authKey :
                    (tokkenArray.length > 5) ? tokkenArray[2] + "-" + tokkenArray[3] + "-" + tokkenArray[4] + "-" + tokkenArray[5] + "-" + tokkenArray[6] : "");
        }
        result.put("tokken", tokken);
        result.put("msg", msg);
        result.put("returnCode", returnCode);

        return result;
    }

    public User getUserInfo(Long userNo){
        Optional<User> userOpt = userRepository.findById(userNo);
        User user = null;

//        if(userOpt != null){ Optional 객체는 null이 절대 안됨! 항상 True인 조건임
        if(userOpt.isPresent()){
            user = userOpt.get();
        }
        return user;
    }




}
