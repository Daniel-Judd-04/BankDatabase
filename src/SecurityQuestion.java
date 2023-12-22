import java.io.Serializable;
import java.util.Arrays;

public class SecurityQuestion implements Serializable {
    private final String id;
    private final String output;
    private final String regex;
    private final Password answer;


    public SecurityQuestion(String id, String output, String regex, Password answer) {
        this.id = id;
        this.output = output;
        this.regex = regex;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return output;
    }

    public boolean checkRegex(String input) {
        return input.matches(regex);
    }

    public boolean checkAnswer(char[] attemptedAnswer) {
        boolean valid = answer.verifyPassword(attemptedAnswer);
        Arrays.fill(attemptedAnswer, '\0');
        return valid;
    }

    public boolean checkAttempts() {
        return answer.checkAttempts();
    }
}
