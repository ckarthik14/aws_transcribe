package com.amazonaws.translation;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

public class TranslateText {
    String source, target;

    public TranslateText(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String translate(String transcript) {
        // Create an AmazonTranslate client
        AmazonTranslate translate = AmazonTranslateClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .withRegion("us-west-2") // specify the region you configured
                .build();

        // Create request
        TranslateTextRequest request = new TranslateTextRequest()
                .withText(transcript)
                .withSourceLanguageCode(source)
                .withTargetLanguageCode(target);

        // Translate the text
        TranslateTextResult result = translate.translateText(request);

        // Output the translation
        System.out.println("Translated text: '" + result.getTranslatedText() + "'");

        return result.getTranslatedText();
    }
}
