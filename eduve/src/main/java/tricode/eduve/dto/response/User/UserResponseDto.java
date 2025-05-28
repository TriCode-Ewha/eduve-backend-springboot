package tricode.eduve.dto.response.User;

import lombok.Builder;
import lombok.Getter;
import tricode.eduve.domain.User;

@Getter
public class UserResponseDto {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String role;

    @Builder
    public UserResponseDto(Long userId, String username, String name, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
