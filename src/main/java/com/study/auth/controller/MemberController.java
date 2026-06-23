package com.study.auth.controller;

import com.study.auth.model.User;
import com.study.auth.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/index/member")
@CrossOrigin(origins = "")
public class MemberController {

    @Autowired
    MemberService memberService;

    @GetMapping("/login")
    public String MemberLoginAction(HttpServletRequest request, HttpServletResponse response, String msg, Model model){
        // MessageDigest 에서 sha256 인스턴스 생성하고 - request에서 session ID값 불러와서 암호화
        // SHA-256 : 해시함수. 입력 데이터를 받아 항상 고정 길이의 결과를 만듦(256비트 == 32바이트)
        // 처음 암호화된 값은 읽을 수 없는 값도 포함되어있기에, 읽을 수 있는 값으로 변경 -> 1) Hex(16진수) 2) Base64

        MessageDigest md;
        StringBuilder sb = new StringBuilder();

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(request.getSession().getId().getBytes()); // 세션 ID를 byte[]로 바꿔 해시 내부 버퍼에 저장

            byte[] bytes = md.digest(); // 해시 계산 -> 결과 : [-12, 34, 56, ...] 32바이트 생성

            for (byte byteObj : bytes) { // 16진수 문자열로 변환 -> e3b0c44298fc1c149afbf4c8996fb924...
                sb.append(Integer.toString((byteObj&0xFF) + 256, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Map<String, Object> returnBase = new HashMap<>();

        returnBase.put("r", sb.toString());

        // 로그인 view 리턴
        model.addAllAttributes(returnBase);
        String path = "/member/login";

        return path;
    }

    @PostMapping("/api/LoginJSON")
    public ResponseEntity<?> LoginJSON(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String ref){

        ref = request.getParameter("ref");
        String reqR = StringUtils.defaultIfEmpty(request.getParameter("r"), StringUtils.EMPTY);
        String path = "/index";
        String msg = "";
        User user = null;

        // 로그인 3초 딜레이
        // Capcha 파라미터 검증 - capcha 없으면 return

        // CSRF 검증 - 사용자가 자신의 의지와는 무관하게 공격자가 의도한 행위(수정, 삭제, 등록 등)를 특정 웹사이트에 요청하게 하는 공격
        // 세션 ID와 파라미터 r 값 비교해서 검증
        String sessionId = request.getSession().getId();
        HashMap<String, Object> returnBase = new HashMap<>();

        System.out.println("you here ????????????????? 1 ");

        if(StringUtils.isNotBlank(sessionId)){
            MessageDigest md;

            try{
                md = MessageDigest.getInstance("SHA-256");
                md.update(sessionId.getBytes());

                byte[] bytes = md.digest();

                StringBuffer sb = new StringBuffer();

                for(byte byteObj : bytes){
                    sb.append(Integer.toString((byteObj&0xFF) + 256, 16).substring(1));
                }

                if(StringUtils.isEmpty(sb.toString()) || !StringUtils.equals(sb.toString(), reqR)){ // CSRF 검증
                    returnBase.put("msg", "invalid access");
                    returnBase.put("url", path);

                    System.out.println("you here ????????????????? 1 ");
                    return ResponseEntity.ok(returnBase);
                }
            } catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("you here ????????????????? 2");
            returnBase.put("msg", "invalid access");
            returnBase.put("url", path);
            return ResponseEntity.ok(returnBase);
        }

        // ref 파라미터 문자열 보안 검사
        // Captcha 통과 여부 판단
        try{
            // 로그인 로직 진행
            Map<String, Object> resultUser = memberService.PortalLoginJSON(request);

            if(!resultUser.isEmpty()){
                int returnCode = (int) resultUser.get("returnCode");
                msg = (String) resultUser.get("msg");

                if(returnCode == 1){
                    Long userNo = (Long) resultUser.get("userNo");

                    // otp 유저는 otp 페이지로 넘어가도록 세팅

                    // user_profile 가져옴(-> 여기선 user 정보 가져오는걸로 대체)
                    user = memberService.getUserInfo(userNo);

                    // wallet 정보 가져옴

                    // authkey + tokkenkey 가져옴
                    Map<String, Object> result = memberService.updateSessionInformation(request, userNo);
                    if((int) result.get("returnCode") == 1){
                        user.setAuthKey((String) result.get("authKey"));
                    }

                    // 세션에 User save
                    request.getSession().removeAttribute("user");
                    request.getSession().setAttribute("user", user);
                }
            } else {
                msg = "Login Failed";
            }

        } catch (Exception e){
            e.printStackTrace();
            msg = "오류 발생. 다시 시도해주세요!";
        }

        returnBase.put("msg", msg);
        returnBase.put("url", path);

        if(msg.equals("success")){
            returnBase.put("returnCode", 1);
        }

        return ResponseEntity.ok(returnBase);
    }



}
