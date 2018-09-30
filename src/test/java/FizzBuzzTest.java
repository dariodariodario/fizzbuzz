
import org.junit.jupiter.api.Test;
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
    public void testChecksAreInTheRightSequence() {
        FizzBuzz fizzBuzz = spy(new FizzBuzz());
        fizzBuzz.decide(2887);
        InOrder inOrder = inOrder(fizzBuzz);
        inOrder.verify(fizzBuzz).isFizzBuzz(2887);
        inOrder.verify(fizzBuzz).isFizz(2887);
        inOrder.verify(fizzBuzz).isBuzz(2887);
    }

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

        program(fizzBuzz);


        Mockito.clearInvocations(fizzBuzz);
        when(fizzBuzz.isFizz(anyInt())).thenReturn(false);
        when(fizzBuzz.isBuzz(anyInt())).thenReturn(true);
        when(fizzBuzz.isFizzBuzz(anyInt())).thenReturn(false);
        fizzBuzz.decide(666);
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);

        Mockito.clearInvocations(fizzBuzz);
        when(fizzBuzz.isFizz(anyInt())).thenReturn(false);
        when(fizzBuzz.isBuzz(anyInt())).thenReturn(false);
        when(fizzBuzz.isFizzBuzz(anyInt())).thenReturn(true);
        fizzBuzz.decide(666);
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);

        Mockito.clearInvocations(fizzBuzz);
        when(fizzBuzz.isFizz(anyInt())).thenReturn(false);
        when(fizzBuzz.isBuzz(anyInt())).thenReturn(false);
        when(fizzBuzz.isFizzBuzz(anyInt())).thenReturn(false);
        fizzBuzz.decide(666);
        verify(fizzBuzz, never()).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz).doNormal(666);
    }

    private void program(FizzBuzz fizzBuzz) {
        when(fizzBuzz.isFizz(anyInt())).thenReturn(true);
        when(fizzBuzz.isBuzz(anyInt())).thenReturn(false);
        when(fizzBuzz.isFizzBuzz(anyInt())).thenReturn(false);
        fizzBuzz.decide(666);
        verify(fizzBuzz).doFizz(666);
        verify(fizzBuzz, never()).doBuzz(666);
        verify(fizzBuzz, never()).doFizzBuzz(666);
        verify(fizzBuzz, never()).doNormal(666);
    }

    @Test
    public void testDecideDontAcceptZeroOrLezz() {
        FizzBuzz fizzBuzz = new FizzBuzz();
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(0));
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(-1));
        assertThrows(IllegalArgumentException.class, () -> fizzBuzz.decide(-1000));
    }


    @Test
    public void testFizzBuzzCallsDecideAndCreatesString() {
        FizzBuzz fizzBuzz = spy(new FizzBuzz());
        AtomicInteger calls = new AtomicInteger();
        doAnswer((Answer<String>) invocationOnMock -> {
            Integer i = invocationOnMock.getArgument(0);
            calls.incrementAndGet();
            return i.toString();
        }).when(fizzBuzz)
                .decide(anyInt());

        int n = positiveRandom();
        String result = fizzBuzz.fizzBuzz(n);
        assertEquals(IntStream.range(1, n + 1).mapToObj(Integer::toString).collect(Collectors.joining(" ")), result);
        assertEquals(n, calls.get());
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