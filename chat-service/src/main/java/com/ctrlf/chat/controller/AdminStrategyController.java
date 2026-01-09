package com.ctrlf.chat.controller;

import com.ctrlf.chat.strategy.StrategyEvent;
import com.ctrlf.chat.strategy.StrategyState;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/strategy")
public class AdminStrategyController {

    @GetMapping("/events")
    public List<StrategyEvent> getStrategyEvents() {
        List<StrategyEvent> events = StrategyState.getEvents();
        int size = events.size();
        return events.subList(Math.max(0, size - 50), size);
    }
}
