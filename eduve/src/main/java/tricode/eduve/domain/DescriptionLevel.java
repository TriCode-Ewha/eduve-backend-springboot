package tricode.eduve.domain;

public enum DescriptionLevel {
    ELEMENTARY,
    MIDDLE,
    HIGH,
    UNIVERSITY,
    EXPERT;

    // 설명 수준에 맞는 프롬프트 문구를 반환하는 메서드
    public String getPromptInstruction() {
        switch (this) {
            case ELEMENTARY:
                return "너는 초등학생이 이해할 수 있도록 아주 간단하고 쉽게 설명해야 해. " +
                        "너무 어려운 단어나 개념을 피하고, 간단한 예시와 일상적인 언어를 사용해.";
            case MIDDLE:
                return "너는 중학생이 이해할 수 있도록 설명해야 해. " +
                        "기본적인 개념은 쉽게 설명하고, 다소 복잡한 개념은 쉽게 풀어서 예시를 들어 설명해야 해.";
            case HIGH:
                return "너는 고등학생이 이해할 수 있도록 설명해야 해. " +
                        "복잡한 개념도 설명할 수 있지만, 여전히 간단한 예시와 함께 친절하게 풀어야 해.";
            case UNIVERSITY:
                return "너는 대학생 수준의 이해를 바탕으로 설명해야 해. " +
                        "좀 더 고차원적인 개념과 용어를 사용할 수 있지만, 여전히 쉽게 풀어 설명하는 것이 중요해.";
            case EXPERT:
                return "너는 전문가 수준의 설명을 해야 해. " +
                        "복잡한 개념과 고급 용어를 사용할 수 있지만, 그에 맞는 깊이 있는 설명과 정확한 정보를 제공해야 해.";
            default:
                return "정확한 설명 수준을 알 수 없습니다.";
        }
    }
}
