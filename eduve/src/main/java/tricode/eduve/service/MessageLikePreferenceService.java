package tricode.eduve.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.MessageLikePreference;
import tricode.eduve.domain.User;
import tricode.eduve.global.ChatGptClient;
import tricode.eduve.repository.MessageLikeRepository;
import tricode.eduve.repository.MessageLikePreferenceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageLikePreferenceService {

    private final MessageLikeRepository messageLikeRepository;
    private final MessageLikePreferenceRepository messageLikePreferenceRepository;
    private final ChatGptClient chatGptClient;

    // 분석하기
    public void runAnalysis(User user) {
        // 좋아요한 메시지 목록 가져오기
        List<String> likedMessages = messageLikeRepository.findAllByUser(user).stream()
                .map(like -> like.getMessage().getContent())
                .toList();

        // ChatGPT로 분석 결과 받아오기
        String result = chatGptClient.analyzeLikedMessages(likedMessages);

        // 해당 사용자에 대한 선호 스타일 정보 가져오기 (없으면 새로 생성)
        MessageLikePreference pref = messageLikePreferenceRepository.findByUser(user)
                .orElse(MessageLikePreference.builder()
                        .user(user) // 사용자 정보 설정
                        .analysisResult(result) // 분석 결과 설정
                        .lastAnalyzedAt(LocalDateTime.now()) // 마지막 분석 시간 설정
                        .build());

        // 새로 생성된 객체는 저장, 기존 객체는 업데이트
        pref.setAnalysisResult(result);
        pref.setLastAnalyzedAt(LocalDateTime.now());
        messageLikePreferenceRepository.save(pref);  // 저장
    }

    // 분석 내용 조회
    public String getAnalysis(User user) {
        return messageLikePreferenceRepository.findByUser(user)
                .map(MessageLikePreference::getAnalysisResult)
                .orElse("아직 분석된 선호 스타일이 없습니다.");
    }
}
