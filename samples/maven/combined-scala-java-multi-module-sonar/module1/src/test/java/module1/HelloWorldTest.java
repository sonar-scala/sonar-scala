package module1;

import static org.junit.Assert.assertEquals;

public class HelloWorldTest {

    @org.junit.Test
    public void testHello() throws Exception {
        assertEquals("Hello", new HelloWorld().hello());
    }
}