package dev.snowdrop.release.exception;

public class JiraGavNotMatchException extends Throwable {
    public JiraGavNotMatchException(String gavText) {
       super(gavText);
    }
}
