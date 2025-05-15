package tricode.eduve.global;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tricode.eduve.domain.AllCharacter;
import tricode.eduve.repository.AllCharacterRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final AllCharacterRepository allCharacterRepository;

    @PostConstruct
    public void initCharacters() {
        // 이미 캐릭터 데이터가 있는 경우, 아무 작업도 하지 않음
        if (allCharacterRepository.count() > 0) return;

        if (allCharacterRepository.count() == 0) {
            List<AllCharacter> defaultCharacters = List.of(
                    new AllCharacter(1L, "용용이", "https://eduve1.s3.ap-northeast-2.amazonaws.com/dragon.webp"),
                    new AllCharacter(2L, "멍멍이", "https://eduve1.s3.ap-northeast-2.amazonaws.com/dog.webp"),
                    new AllCharacter(3L, "다람이", "https://eduve1.s3.ap-northeast-2.amazonaws.com/squirrel.webp"),
                    new AllCharacter(4L, "냥냥이", "https://eduve1.s3.ap-northeast-2.amazonaws.com/cat.webp"),
                    new AllCharacter(5L, "감자", "https://eduve1.s3.ap-northeast-2.amazonaws.com/potato.webp")
            );

            allCharacterRepository.saveAll(defaultCharacters);
        }
    }
}
