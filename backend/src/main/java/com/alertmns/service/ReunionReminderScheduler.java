package com.alertmns.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReunionReminderScheduler {

    private final ReunionService reunionService;

    @Scheduled(cron = "${alertmns.reunions.reminders.cron:0 * * * * *}")
    public void runAutomaticMeetingReminders() {
        try {
            int sentCount = reunionService.sendPendingAutomaticReminders();
            if (sentCount > 0) {
                log.info("Automatic meeting reminders sent for {} reunion(s)", sentCount);
            }
        } catch (Exception e) {
            log.error("Failed to send automatic meeting reminders", e);
        }
    }
}
