package com.dipsource.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.image.ImageUploadResult;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminEventImageControllerTest {

    @Mock
    private ObjectProvider<ImageService> imageServiceProvider;
    @Mock
    private ImageService imageService;
    private MockMvc mvc;

    private final MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AdminEventImageController(imageServiceProvider))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 이미지를_events_폴더에_올리고_주소를_반환한다() throws Exception {
        when(imageServiceProvider.getIfAvailable()).thenReturn(imageService);
        when(imageService.upload(any(), eq("events")))
                .thenReturn(new ImageUploadResult("https://cdn.example.com/events/a.jpg", "https://cdn.example.com/events/a_thumb.jpg"));

        mvc.perform(multipart("/admin/events/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.url").value("https://cdn.example.com/events/a.jpg"));
    }

    @Test
    void 이미지_저장소가_꺼져_있으면_400이다() throws Exception {
        when(imageServiceProvider.getIfAvailable()).thenReturn(null);

        mvc.perform(multipart("/admin/events/images").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
