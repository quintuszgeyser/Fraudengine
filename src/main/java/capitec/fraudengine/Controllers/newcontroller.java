package capitec.fraudengine.Controllers;

import org.springframework.web.bind.annotation.RestController;

import capitec.fraudengine.Classes.Greetings;
import capitec.fraudengine.Classes.Transaction;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class newcontroller {
    @GetMapping("hello")
    public Greetings ResposnseEntity(@RequestParam String param) {
        return new Greetings(param, param);
    }

    @GetMapping("GetTransaction")
    public Transaction getTransaction(@RequestParam Long id, @RequestParam String card_acceptor) {
        String now = LocalDateTime.now().toString();

        return new Transaction(id, now, card_acceptor);

    }

}
