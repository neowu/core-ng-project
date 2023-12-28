package core.log.web;

import core.framework.api.http.HTTPStatus;
import core.framework.async.Executor;
import core.framework.inject.Inject;
import core.framework.web.Request;
import core.framework.web.Response;
import core.log.IntegrationTest;
import core.log.service.ArchiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class UploadControllerTest extends IntegrationTest {
    @Mock
    Request request;
    @Mock
    ArchiveService archiveService;
    @Inject
    Executor executor;

    private UploadController controller;

    @BeforeEach
    void createUploadController() {
        when(request.bean(UploadRequest.class)).thenReturn(new UploadRequest());
        controller = new UploadController();
        controller.executor = executor;
        controller.archiveService = archiveService;
    }

    @Test
    void execute() {
        Response response = controller.execute(request);

        assertThat(response.status()).isEqualTo(HTTPStatus.NO_CONTENT);
        verify(archiveService).uploadArchive(LocalDate.now());
    }
}
