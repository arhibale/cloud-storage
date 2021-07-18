package model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class Message implements Serializable {

    private Date sendAt;
    private String content;
    private String[] contentM;

    public Message(String content) {
        this.content = content;
        sendAt = new Date();
    }

    public Message(String[] contentM) {
        this.contentM = contentM;
    }

    public Date getSendAt() {
        return sendAt;
    }

    public String getContent() {
        return content;
    }

    public String[] getContentM() {
        return contentM;
    }
}
