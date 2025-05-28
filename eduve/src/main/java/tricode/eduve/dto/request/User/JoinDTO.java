package tricode.eduve.dto.request.User;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinDTO {
    private String name;
    private String username;
    private String password;
    private String email;
    private String teacherUsername;
}