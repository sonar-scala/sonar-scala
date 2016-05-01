import module2.HelloWorld2;

import static org.junit.Assert.assertEquals;

public class HelloWorld2Test {

    @org.junit.Test
    public void testHello() throws Exception {
        assertEquals("Hello", new HelloWorld2().hello());
    }
}