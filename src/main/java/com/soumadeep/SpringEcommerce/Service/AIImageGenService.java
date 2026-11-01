package com.soumadeep.SpringEcommerce.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AIImageGenService {

    private final ImageModel imageModel;

    public byte[] generateImage(String prompt) {

        OpenAiImageOptions options= OpenAiImageOptions.builder()
                .height(1024)
                .width(1024)
                .quality("medium")
                .build();

        ImageResponse response=imageModel.call(new ImagePrompt(prompt,options));
        String image_b64=response.getResult().getOutput().getB64Json();
        return Base64.getDecoder().decode(image_b64);
    }
}
