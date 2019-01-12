# fizzbuzz
## My attempt at a proper fizzbuzz implementation in Java TDD.

The fizzbuzz problem:

given a number n, output a string where all numbers from 1 to n are output and they are rendered according to:

- if multiple of 3 as fizz
- if multiple of 5 as buzz
- if multiple of 3 and 5 as fizzbuzz
- otherwise just the decimal number.

This test is of simple implementation, but it is hard to test it rigorously with TDD. This is my solution.

##Explanation

After an heated debate with a colleague, I thought it's worth writing an explanation for this implementation.

Let's start by looking at the implementation of [fizzbuzz](src/main/java/FizzBuzz.java). It's surely looks a bit verbose maybe. Also looks highly decomposed. 
The same functionality, with the same logic could surely be expresses in a much more concise way. The reason of this structure is to make the code highly testable. In practice, the solution has been decomposed in a way that gives more certainty of the program actual result.

We can start by putting the simplest test for our implementation:

```
  @Test
    public void simpleFizzBuzzTest(){
        FizzBuzz fizzBuzz = new FizzBuzz();
        assertEquals("1 2 fizz 4 buzz fizz 7 8 fizz buzz 11 fizz 13 14 fizzbuzz 16", fizzBuzz.fizzBuzz(16));
    } 
```

this includes all the business rules, at least once. But this is not exhaustive for a number of reasons:

- what happens if the argument is <= 0?
- a function that returns the string of the example will pass the test. Now, this is a bit silly as an argument, because you are the programmer writing this code, so why would you cheat yourself in that way?
- are we sure that the implementation works for n > 16 ?

Point 3 is particularly interesting. The **input space of this function is very large**: half of the integers. So maybe writing a particularly long string, several gigabytes in size, could give confidence? No of course, not even going to such lengths would. In fact that doesn't prove that your code will work for smaller numbers.

Somehow we must test this, in a satisfying way and be sure that _it covers the entire input space_.

One may have the brilliant idea of writing a test that generates a random valid input number, then generates the expected string, then uses that as a test case. Congratulations, you just reimplemented the logic of fizzbuzz twice :)

## Decomposition

There must be some other way. And that way is: let's decompose our implementation in layers of functionality, so that we can test each layer in a very simple way, giving for granted that its dependencies are correct. Eventually, we will get to some fundamental building blocks of functionality, that are easy as well to test and we will have proven that the entire chain of trust is valid.

The test above still holds, it must pass, so let's leave it in our suite. But let's start thinking of an implementation where I can decompose logically the functionality:

```
public String fizzBuzz(int n) {
        return IntStream.range(1, n+1)
                .mapToObj(this::decide)
                .collect(Collectors.joining(" "));
    }
```

First thing that I am asked is to create a string, that contains all numbers from 1 to n and that some of them are transformed according to a business rule.

For now I want to concentrate myself on the string generating part, without testing the business rule. So I introduce a function, called `decide` that takes a number and gives me a string representation. The nature of fizzbuzz problem means that this is enough to decide the business logic.

What I want to test here is that given a number, a string is created, by passing the increasing numbers from 1 to n to a function called decide and stitching the results together.

So I can test that:
1. for N decide is called in order N times, with always increasing numbers.
2. that the results of decide are stitched together.

So for 1 we can write:

```
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
```

as you see, we don't care what the return value of decide is. We just care about its argument values and their order.

For 2:

```
@Test
    public void testFizzBuzzStitchesTogetherTheResultOfDecide(){
        FizzBuzz fizzBuzz = spy(new FizzBuzz());
        doReturn("a")
                .doReturn("b")
                .doReturn("c")
                .when(fizzBuzz).decide(anyInt());

        assertEquals("a b c", fizzBuzz.fizzBuzz(3));
    }
``` 

this is a bit less complete and does not cover all input space... Yet above we are guaranteed that given any number, it will call decide N times in order. So we can trust that for the input space. Here I just want to check that the result are joined together.


A this point I can say that the function fizzbuzz is correct *in isolation*. I need to implement decide now and test it. If it is correct, it automatically makes the whole of fizzbuzz solution correct.

I could go and implement decide as a do it all block like:

```
String decide(int n) {
        if (n <= 0){
            throw new IllegalArgumentException();
        }
        
        if (n % 3 == 0 && n % 5 == 0){
            return "fizzbuzz";
        }else if (n % 5 == 0){
            return "buzz";
        }else if (n % 3 == 0){
            return "fizz";
        }else{
            return Integer.toString(n);
        }
    }
```

but how can I write satisfying tests for it? Once again either I re-implement the functionality in the test or I use a table of examples. What I want once again is to test the logical rules of this function. 
If we look closely I can tell that the function:
- Applies in order a series of test / output
- If a test passes, it will return it's output and skip the rest
- If no test passes just outputs the number as a string.

then on a lower level there are the rules about the divisibility of the input numbers.

If I remove another level of logic, I can furtherly decompose in:

```
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
```

where I can just test the logic stated now and not care of the lower level of the implementation. Once again I simplified the testing by abstracting the implementation logic.

```
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
```

Here I just program the stubbed test methods and see that they are called according to the chain logic.
Also I test that the result of the linked doXXX method is returned. This test could have been done in a separate case, but I put it here not to make it too overkill.

Finally I'm left with testing and implementing the isXXX and doXXX methods. Testing doXXX is trivial as they always return simple values, probably I can be happy with the last test case.

The problem is testing the isFizz, isBuzz, isFizzBuzz methods. They have a large input. But unlike the full fizzbuzz program, here the laws governing them are rather simple. 

I decided to test them this way:

```
@Test
    public void testChecks() {
        FizzBuzz fizzBuzz = new FizzBuzz();
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isFizz(multipleOf(3))));
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isBuzz(multipleOf(5))));
        IntStream.range(1, 100).forEach(i -> assertTrue(fizzBuzz.isFizzBuzz(multipleOfBoth(3, 5))));
    }
```

I cannot cover all of the input space, but I can be pretty sure that, if throw 100 random numbers, that are the right multiple to trigger of the testing functions, then the test functions work the right way.

Wrapping it all up together, I have written my code in order to create simple layers of logic, that can be exhaustively tested. What I have, is a proof chain:

    fizzbuzz -> decide -> isXXX / doXXX

if every step is proven with a simple test, because of the decomposition and the logical dependency I introduced, it means that as a whole the program is correct.

Eventually the main "black box" test that I written initially will pass.

To recap a bit the core principles of decomposition and TDD:

- we must decompose the implementation every time that it is not possible to test it exhaustively. It must be possible to mock/stub the sub components and the test/implementation must give for granted their correctness.
- every time it's difficult or confusing or lengthy to create a test case to test a subcomponent, one must find a way to decompose the implementation to remove such complexity.

 