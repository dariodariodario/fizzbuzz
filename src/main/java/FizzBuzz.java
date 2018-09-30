
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FizzBuzz {

    String doFizz(int n) {
        return "fizz";
    }

    String doBuzz(int n) {
        return "buzz";
    }

    String doFizzBuzz(int n) {
        return "fizzbuzz";
    }

    String doNormal(int n) {
        return Integer.toString(n);
    }

    boolean isFizz(int n) {
        return n % 3 == 0;
    }

    boolean isBuzz(int n) {
        return n % 5 == 0;
    }

    boolean isFizzBuzz(int n) {
        return n % 3 == 0 && n % 5 == 0;
    }


    String decide(int n) {
        if (n <= 0){
            throw new IllegalArgumentException();
        }
        if (isFizzBuzz(n)) {
            return doFizzBuzz(n);
        } else if (isFizz(n)) {
            return doFizz(n);
        } else if (isBuzz(n)) {
            return doBuzz(n);
        } else {
            return doNormal(n);
        }
    }

    public String fizzBuzz(int n) {
        return IntStream.range(1, n+1)
                .mapToObj(this::decide)
                .collect(Collectors.joining(" "));
    }


}
