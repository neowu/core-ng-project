package core.framework.internal.log;

// return tracking result, so caller can decide if gather additional info to help troubleshooting
public record TrackResult(int count, boolean slow) {
}
