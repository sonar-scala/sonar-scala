import static org.junit.Assert.assertEquals;

public class JavaExampleTest {
    @org.junit.Test
    public void testSum() {
        assertEquals(new JavaExample().sum(1, 5), 6);
    }
}
