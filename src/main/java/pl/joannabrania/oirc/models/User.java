package pl.joannabrania.oirc.models;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

public class User {
    private String nickname;
    private WebSocketSession session;

    public User(WebSocketSession session){
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(nickname, user.nickname) &&
                Objects.equals(session.getId(), user.session.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(nickname, session.getId());
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
