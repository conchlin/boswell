package net.server.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tools.Pair;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpWorker {
    public Gson gson = new Gson();
    private BlockingQueue<Pair<String, String>> q = new LinkedBlockingQueue<>();

    private Thread thread;
    public HttpWorker() {
        thread = new Thread(new WorkerThread(q));
        thread.start();
    }

    public void add(String dest) {
        add(dest, "{}");
    }

    public void add(String dest, String data) {
        System.out.println("HTTP " + dest + ", " + data);
        try {
            for (Pair<String, String> p : q) {
                if (p.left.equals(dest) && p.right.equals(data)) return;
            }
            q.add(new Pair<>(dest, data));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        if (!thread.isAlive()) {
            thread = new Thread(new WorkerThread(q));
            thread.start();
        }
    }

    public void add(String dest, JsonObject data) {
        add(dest, data.toString());
    }

    private static class WorkerThread implements Runnable {
        private final BlockingQueue<Pair<String, String>> queue;

        WorkerThread(BlockingQueue<Pair<String, String>> q) {
            queue = q;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Pair<String, String> value = queue.take();
                    try {
                        URL url = new URL(value.left);
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
                        http.setRequestMethod("POST");
                        http.setDoOutput(true);
                        http.setRequestProperty("Content-Type", "application/json; utf-8");
                        try (OutputStream os = http.getOutputStream()) {
                            byte[] input = value.right.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }
                        http.getResponseCode();
                        http.disconnect();
                    } catch (Exception e) {
                        System.out.println("failed to connect to web server");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
