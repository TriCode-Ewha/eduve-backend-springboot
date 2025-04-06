package tricode.eduve.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tricode.eduve.dto.JoinDTO;
import tricode.eduve.service.StudentJoinService;

import java.util.HashMap;
import java.util.Map;


@RestController
@ResponseBody
public class StudentJoinController {

    private final StudentJoinService studentJoinService;

    public StudentJoinController(StudentJoinService studentJoinService) {

        this.studentJoinService = studentJoinService;
    }

    @PostMapping("/join/student")
    public ResponseEntity<Map<String, Object>> joinProcess(@RequestBody JoinDTO joinDTO) {

        System.out.println(joinDTO.getUsername());
        studentJoinService.joinProcess(joinDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다.");
        // response.put("userId", joinDTO.getUsername());

        return ResponseEntity.ok(response);
    }
}
