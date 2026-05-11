package sk.tuke.gamestudio.server.webservice;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameSseBroadcaster {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long gameId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(gameId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(gameId, emitter));
        emitter.onTimeout(()   -> remove(gameId, emitter));
        emitter.onError(e      -> remove(gameId, emitter));
        return emitter;
    }

    public void broadcast(Long gameId, Object state) {
        List<SseEmitter> list = emitters.getOrDefault(gameId, new CopyOnWriteArrayList<>());
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try { emitter.send(state); }
            catch (IOException e) { dead.add(emitter); }
        }
        list.removeAll(dead);
    }

    private void remove(Long gameId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(gameId);
        if (list != null) list.remove(emitter);
    }
}
