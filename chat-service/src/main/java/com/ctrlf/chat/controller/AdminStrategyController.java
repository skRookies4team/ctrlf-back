package com.ctrlf.chat.controller;

import com.ctrlf.chat.strategy.StrategyState;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/strategy")
public class AdminStrategyController {

    @GetMapping("/events")
    public List<Map<String, Object>> getStrategyEvents() {
        int size = StrategyState.STRATEGY_EVENTS.size();
        return StrategyState.STRATEGY_EVENTS
            .subList(Math.max(0, size - 50), size);
    }
}
