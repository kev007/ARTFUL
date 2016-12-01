
package fmdir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Server {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Server.class, args);

        //FreqService wordService = ctx.getBean("wordServiceImpl", FreqService.class);
        //System.out.println(wordService.getAllFreqs());

    }
}
