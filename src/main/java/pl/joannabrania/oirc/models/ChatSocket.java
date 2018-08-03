package pl.joannabrania.oirc.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

//musimy uruchomic websocket:
@EnableWebSocket
@Component //component Springowski (ziarno )
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private Set<User> users = new HashSet<>();
    private LinkedList<TextMessage> allMessages = new LinkedList<>();   //lista podłączonych


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry){
        webSocketHandlerRegistry.addHandler(this,"/chat")           //mówimy: masz wziac rejest , dodac Handlet( this cyli ta cała klasa),
                                                  .setAllowedOrigins("*");                  // ten handler ma byc dyspozycyjny pod linkiem /chat
                                                                                            //wszystkie adresy API mogą połączyc się z socketem
    }

    @Override   //co sie stanie jak ktos podłaczy się do socketa
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        users.add(new User(session));

        User connectedUser = findUSerBySession(session);
        connectedUser.getSession().sendMessage(new TextMessage("Witaj na czacie!: )"));
        connectedUser.getSession().sendMessage(new TextMessage("Twoja pierwsza wiadomość będzie Twoim nickiem"));
        for (TextMessage allMessage : allMessages) {
            connectedUser.getSession().sendMessage(allMessage);
        }

    }

    @Override    //co się stani jak ktos wysle wiadomosc
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User sender = findUSerBySession(session);

        if(sender.getNickname() == null ) {
            if(!isNicknameFree(message.getPayload())){
                sender.getSession().sendMessage(new TextMessage("Nick zajęty. Podaj inny"));
                return;
            }
            sender.setNickname(message.getPayload());           //getPeyload wypluwa wiadomosc, wiadomsoc uzutkownika
            sender.getSession().sendMessage(new TextMessage("Ustawiliśmy Twój nick"));
            return;
        }

       sendWelcomeMessage(session,sender);
       sendMessageToAllUsers(createMessageWithSenderNickname(message, sender));
    }

    private boolean isNicknameFree(String nickname) {
        return users.stream().noneMatch(s -> s.getNickname() != null && s.getNickname().equals(nickname));
    }

    private TextMessage createMessageWithSenderNickname(TextMessage message, User sender) {
        TextMessage text = new TextMessage(sender.getNickname()+": "+ message.getPayload());
        if(allMessages.size() <10 )
                allMessages.add(text);
        else {
            allMessages.removeFirst();
            allMessages.addLast(text);
        }
        return text;
    }

    private User findUSerBySession(WebSocketSession session) {
        return users.stream()
                .filter(s -> s.getSession().getId().equals(session.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    private void sendMessageToAllUsers(TextMessage message) throws IOException {
        for ( User user : users){
            user.getSession().sendMessage(message);
        }
    }

    private void sendWelcomeMessage( WebSocketSession session,  User sender) throws IOException {
        for ( User user : users){
            if(user.getSession().getId() != sender.getSession().getId() )
            user.getSession().sendMessage(new TextMessage("Nowy uczestnik: "+sender.getNickname()));
        }}

     @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            users.stream()
                    .filter(s -> s.getSession().getId().equals(session.getId()))
                    .findAny()
                    .ifPresent(s -> users.remove(s));
        }
}
