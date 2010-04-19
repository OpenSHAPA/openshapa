package org.openshapa.event.component;

import java.util.EventObject;


/**
 * Event object for a track carriage event.
 */
public final class CarriageEvent extends EventObject {

    /**
     * Auto generated by Eclipse.
     */
    private static final long serialVersionUID = 4009420871939032673L;

    /** Describes the type of event that occured. */
    public enum EventType {

        /** Snap position changed. */
        BOOKMARK_CHANGED,

        /** Requesting snap position. */
        BOOKMARK_REQUEST,

        /** Requesting snap saving. */
        BOOKMARK_SAVE,

        /** Track changed selection state. */
        CARRIAGE_SELECTION,

        /** Track changed offset position. */
        OFFSET_CHANGE
    }

    /** Track identifier. */
    private final String trackId;

    /** New time offset, in milliseconds, for the given track. */
    private final long offset;

    /** Duration of the track in milliseconds. */
    private final long duration;

    /** Track bookmark position in milliseconds. */
    private final long bookmark;

    /**
     * Temporal position of the mouse. Only meaningful for
     * EventType.OFFSET_CHANGE
     */
    private final long temporalPosition;

    /** What event does this represent. */
    private final EventType eventType;

    /** Were there input modifiers. */
    private final boolean hasModifiers;

    /**
     * @param source
     * @param trackId
     * @param offset
     * @param bookmark
     * @param duration
     * @param temporalPosition
     * @param eventType
     * @param hasModifiers;
     */
    public CarriageEvent(final Object source, final String trackId,
        final long offset, final long bookmark, final long duration,
        final long temporalPosition, final EventType eventType,
        final boolean hasModifiers) {
        super(source);
        this.trackId = trackId;
        this.offset = offset;
        this.bookmark = bookmark;
        this.duration = duration;
        this.temporalPosition = temporalPosition;
        this.eventType = eventType;
        this.hasModifiers = hasModifiers;
    }

    /**
     * @return New time offset in milliseconds.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @return the bookmark
     */
    public long getBookmark() {
        return bookmark;
    }

    /**
     * @return Track identifier
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * @return Track duration in milliseconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return the temporal position of the mouse in milliseconds. Only
     *         meaningful for OFFSET_CHANGE events.
     */
    public long getTemporalPosition() {
        return temporalPosition;
    }

    /**
     * @return event type
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @return true if modifiers were present, false otherwise..
     */
    public boolean hasModifiers() {
        return hasModifiers;
    }

}
