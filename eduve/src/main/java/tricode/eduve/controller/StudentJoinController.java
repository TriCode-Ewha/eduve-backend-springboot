package tricode.eduve.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tricode.eduve.domain.User;
import tricode.eduve.dto.JoinDTO;
import tricode.eduve.dto.response.JoinResponseDto;
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
    public ResponseEntity<JoinResponseDto> joinProcess(@RequestBody JoinDTO joinDTO) {

        System.out.println(joinDTO.getUsername());
        User savedUser = studentJoinService.joinProcess(joinDTO);

        JoinResponseDto response = JoinResponseDto.builder()
                .joinDto(joinDTO)
                .userId(savedUser.getUserId())
                .build();

        return ResponseEntity.ok(response);
    }
}
