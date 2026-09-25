import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainServer {
    public static void main(String[] args) throws IOException {
        // Starts the server listening on network port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // This handles what happens when you load http://localhost:8080
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // We changed this path to look directly for your simple name!
                String filePath = "src/webapp/login.html";
                try {
                    byte[] response = Files.readAllBytes(Paths.get(filePath));
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } catch (IOException e) {
                    String errorMsg = "Error: Cannot find the web file at " + filePath;
                    exchange.sendResponseHeaders(404, errorMsg.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorMsg.getBytes());
                    os.close();
                }
            }
        });

        System.out.println("==========================================================");
        System.out.println(" CareerConnect Server is running.");
        System.out.println(" Open this link in your browser: http://localhost:8080");
        System.out.println("==========================================================");
        server.start();
    }
}
