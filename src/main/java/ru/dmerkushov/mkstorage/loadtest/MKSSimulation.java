package ru.dmerkushov.mkstorage.loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.log4j.Log4j2;
import ru.dmerkushov.loadtest.LoadSimulation;
import ru.dmerkushov.loadtest.LoadTestActionResult;

@Log4j2
public class MKSSimulation implements LoadSimulation {

    private HttpClient httpClient;
    private HttpRequest setupRequest;
    private HttpRequest actionRequest;

    private final String uriStr = "http://localhost:8080/testing/tag1,tag2";
    private final String testXmlStr = "<myXml><someOne/></myXml>";

    public MKSSimulation() {
        try {
            httpClient = HttpClient.newHttpClient();
            setupRequest = HttpRequest.newBuilder()
                    .uri(new URI(uriStr))
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(testXmlStr))
                    .build();
            actionRequest = HttpRequest.newBuilder()
                    .uri(new URI(uriStr))
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .GET()
                    .build();
        } catch (Exception e) {
            log.warn(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepare() {
        try {
            httpClient.send(setupRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.warn(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void over() {
        // Do nothing
    }

    @Override
    public void action(AtomicReference<LoadTestActionResult.LoadTestActionStatus> actionResultAtomicReference) {
        actionResultAtomicReference.set(LoadTestActionResult.LoadTestActionStatus.PARTIAL_1);

        try {
            HttpResponse<String> response = httpClient.send(actionRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                actionResultAtomicReference.set(LoadTestActionResult.LoadTestActionStatus.PARTIAL_2);
                if (response.body().equals(testXmlStr)) {
                    actionResultAtomicReference.set(LoadTestActionResult.LoadTestActionStatus.SUCCESS);
                    return;
                }
            }
        } catch (Exception e) {
            log.warn(e);
            actionResultAtomicReference.set(LoadTestActionResult.LoadTestActionStatus.FAIL);
        }

    }
}
