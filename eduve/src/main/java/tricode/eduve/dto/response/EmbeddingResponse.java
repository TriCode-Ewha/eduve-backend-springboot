package tricode.eduve.dto.response;

public class EmbeddingResponse {
    private String message;
    private String text;

    // 기본 생성자, getter, setter 필요
    public EmbeddingResponse() {}
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
