package jlox;

public class Return extends RuntimeException {
  public final Object value;

  public Return(Object value) {
    super(null, null, false, false); // Disable JVM stack trace generation
    this.value = value;
  }
}