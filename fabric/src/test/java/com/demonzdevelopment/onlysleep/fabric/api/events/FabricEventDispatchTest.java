package com.demonzdevelopment.onlysleep.fabric.api.events;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricEventDispatchTest {

    @Test
    void sleepStartEventDispatchesAndCanBeCancelled() {
        AtomicBoolean called = new AtomicBoolean();
        SleepStartEvent.EVENT.register(event -> {
            called.set(true);
            event.setCancelled(true);
        });

        SleepStartEvent event = new SleepStartEvent(null, 1, 1);
        SleepStartEvent.EVENT.invoker().onSleepStart(event);

        assertTrue(called.get());
        assertTrue(event.isCancelled());
    }

    @Test
    void nightSkipEventDispatchesAndCanBeCancelled() {
        AtomicBoolean called = new AtomicBoolean();
        NightSkipEvent.EVENT.register(event -> {
            called.set(true);
            event.setCancelled(true);
        });

        NightSkipEvent event = new NightSkipEvent(null, null, 1, 1, 1);
        NightSkipEvent.EVENT.invoker().onNightSkip(event);

        assertTrue(called.get());
        assertTrue(event.isCancelled());
    }

    @Test
    void sleepCancelEventDispatches() {
        AtomicBoolean called = new AtomicBoolean();
        SleepCancelEvent.EVENT.register(event -> called.set(true));

        SleepCancelEvent event = new SleepCancelEvent(
            null, SleepCancelEvent.Cause.BED_LEAVE, 0, 1);
        SleepCancelEvent.EVENT.invoker().onSleepCancel(event);

        assertTrue(called.get());
    }
}
