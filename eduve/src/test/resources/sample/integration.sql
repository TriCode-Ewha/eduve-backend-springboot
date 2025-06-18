-- 🔧 외래 키 무시하고 초기화
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_character;
TRUNCATE TABLE preference;
TRUNCATE TABLE all_character;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 👤 사용자 삽입
INSERT INTO user (user_id, name, username, password, email, role, teacher_username)
VALUES
    (100, '김선생', 'teacher01', 'password123', 'teacher01@example.com', 'ROLE_TEACHER', NULL),
    (101, '이학생', 'student01', 'password456', 'student01@example.com', 'ROLE_STUDENT', 'teacher01');

-- 🧠 캐릭터 목록 삽입 (all_character 존재할 경우)
INSERT INTO all_character (all_character_id, character_name)
VALUES (1, '기본 캐릭터');

-- 🎯 preference 먼저 삽입
INSERT INTO preference (preference_id, tone, description_level)
VALUES (200, 'FRIENDLY', 'HIGH');

-- 🧍 user_character 연결 (user_id = 101, character_id = 1, preference_id = 200)
INSERT INTO user_character (
    user_character_id, user_id, character_id, user_character_name, preference_id
) VALUES (
             100, 101, 1, '공감형 조언자', 200
         );