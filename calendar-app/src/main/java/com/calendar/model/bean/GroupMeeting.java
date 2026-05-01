package com.calendar.model.bean;

import java.util.ArrayList;
import java.util.List;

public class GroupMeeting extends Appointment {
    private List<Long> participantIds = new ArrayList<>();

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
}
