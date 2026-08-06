package pe.laherradura.dto;

public record ChatMessageResponse(
        String reply,
        String state,
        String orderCode,
        boolean humanHandoff,
        String mediaUrl,
        String mediaType
) {
    public ChatMessageResponse(String reply, String state, String orderCode, boolean humanHandoff) {
        this(reply, state, orderCode, humanHandoff, null, null);
    }
}
