
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class FizzBuzzTest {

    static final Random random = new Random(System.currentTimeMillis());

    @Test
    public void testChecks() {
        FizzBuzz fizzBuzz = new FizzBuzz();
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isFizz(multipleOf(3))));
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isBuzz(multipleOf(5))));
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isFizzBuzz(multipleOfBoth(3, 5))));
    }

    @Test
    public void testCheckCallsRightTransform() {
        FizzBuzz fizzBuzz = spy(new FizzBuzz());

        setTestsValues(fizzBuzz, true, false, false);
        assertEquals("fizz", fizzBuzz.decide(666));
        verify(fizzBuzz).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);

        setTestsValues(fizzBuzz, false, true, false);
        assertEquals("buzz", fizzBuzz.decide(666));
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);

        setTestsValues(fizzBuzz, false, false, true);
        assertEquals("fizzbuzz", fizzBuzz.decide(666));
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);

        setTestsValues(fizzBuzz, false, false, false);
        assertEquals("666", fizzBuzz.decide(666));
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz).doNormal(666);
    }

    private void setTestsValues(FizzBuzz fizzBuzz, boolean fizz, boolean buzz, boolean fizzbuzz) {
        Mockito.reset(fizzBuzz);
        Mockito.clearInvocations(fizzBuzz);
        doReturn(fizz).when(fizzBuzz)
                .isFizz(anyInt());
        doReturn(buzz).when(fizzBuzz)
                .isBuzz(anyInt());
        doReturn(fizzbuzz).when(fizzBuzz)
                .isFizzBuzz(anyInt());
    }


    @Test
    public void testDecideDontAcceptZeroOrLezz() {
        FizzBuzz fizzBuzz = new FizzBuzz();
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(0));
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(-1));
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(-1000));
    }


    @Test
    public void testFizzBuzzCallsDecideRightOrder() {
        FizzBuzz fizzBuzz = spy(new FizzBuzz());
        int n = positiveRandom();
        doReturn("").when(fizzBuzz).decide(anyInt());

        fizzBuzz.fizzBuzz(n);

        ArgumentCaptor<Integer> intAc = ArgumentCaptor.forClass(Integer.class);
        verify(fizzBuzz, times(n)).decide(intAc.capture());

        IntStream.range(1, n + 1)
                .forEach(i -> assertEquals(i, (int) intAc.getAllValues().get(i - 1)));
    }

    @Test
    public void testFizzBuzzStitchesTogetherTheResultOfDecide(){
        FizzBuzz fizzBuzz = spy(new FizzBuzz());
        doReturn("a")
                .doReturn("b")
                .doReturn("c")
                .when(fizzBuzz).decide(anyInt());

        assertEquals("a b c", fizzBuzz.fizzBuzz(3));
    }

    @Test
    public void testHappy() {
        FizzBuzz fizzBuzz = new FizzBuzz();
        String result = fizzBuzz.fizzBuzz(15);
        assertEquals("1 2 fizz 4 buzz fizz 7 8 fizz buzz 11 fizz 13 14 fizzbuzz", result);

    }

    private int positiveRandom() {
        return Math.abs(random.nextInt(500));
    }

    private int multipleOf(int i) {
        return random.nextInt(Integer.MAX_VALUE / i) * i;
    }

    private int multipleOfBoth(int one, int two) {
        while (true) {
            int om = random.nextInt(Integer.MAX_VALUE / one) * one;
            if (om % two == 0) {
                return om;
            }
        }
    }
}