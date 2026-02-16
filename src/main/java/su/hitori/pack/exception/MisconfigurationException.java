package su.hitori.pack.exception;

public class MisconfigurationException extends RuntimeException {

    private final String trouble;

    public MisconfigurationException(String trouble) {
        super();
        this.trouble = trouble;
    }

    public String formattedMessage(String configurable) {
        return formattedMessage(configurable, trouble);
    }

    private static String formattedMessage(String configurable, String trouble) {
        return String.format("%s contains a problem in configuration: \"%s\"", configurable, trouble);
    }

}
