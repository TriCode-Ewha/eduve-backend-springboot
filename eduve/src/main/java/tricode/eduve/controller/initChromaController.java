package tricode.eduve.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tricode.eduve.service.initChromaService;

@RestController
@RequestMapping("/initChroma")
public class initChromaController {
    private final initChromaService initChromaService;

    public initChromaController(initChromaService initChromaService) {
        this.initChromaService = initChromaService;
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAll(@RequestParam Long userId) {
        try {
            initChromaService.callDeleteApi(userId);
            return ResponseEntity.ok("삭제 요청 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 요청 실패: " + e.getMessage());
        }
    }
}
