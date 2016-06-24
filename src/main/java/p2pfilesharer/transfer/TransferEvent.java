package p2pfilesharer.transfer;

import com.google.common.collect.Range;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nick
 */
public class TransferEvent {

    public enum EventType {
        CREATE, STAGE_CHANGE, STATE_CHANGE, PROGRESS, RESET, ERROR;
    }

    
    EventType eventType;
    Transfer download;
    Range<Long> result;
    Exception error;
    
    public TransferEvent(EventType eventType, Transfer download) {
        this.eventType = eventType;
        this.download = download;
    }

    public TransferEvent(Transfer download, Range<Long> result) {
        this.eventType = EventType.PROGRESS;
        this.download = download;
        this.result = result;
    }
    
    public TransferEvent(Transfer download, Exception error) {
        this.eventType = EventType.ERROR;
        this.download = download;
        this.error = error;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Transfer getFuture() {
        return download;
    }

    public Range<Long> getResult() {
        return result;
    }
}
