
package duck.moon.sample.bean;

import org.springframework.stereotype.Component;

@Component
public class SampleService {
  public void printSample() {
    System.out.println("sample!");
  }
}
