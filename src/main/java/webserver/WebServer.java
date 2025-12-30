package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.

        int cores = Runtime.getRuntime().availableProcessors();
        int threadpool_size = cores * 2;
        logger.info(" Now Available Cores : {} , Thread pool Size : {} ",cores , threadpool_size);

        ExecutorService executorService = Executors.newFixedThreadPool(threadpool_size);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            Socket connection;

            while (true) {
                connection = listenSocket.accept();
                if(connection != null){
                    executorService.execute(new RequestHandler(connection));
                }
                // 기존 thread 직접 생성 버전
                //Thread thread = new Thread(new RequestHandler(connection));
                //thread.start();
            }
        }finally {
            logger.info("terminate thread pool" );
            executorService.shutdown();
        }
    }
}
