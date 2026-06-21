package com.example.SprinAICode;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

//import javax.swing.text.Document;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private VectorStore  vectorStore;

    @PostConstruct
    public void initData(){
        TextReader textReader = new TextReader(new ClassPathResource("productDetails.txt"));

        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        TokenTextSplitter splitter1 = TokenTextSplitter.builder()
                .withChunkSize(100)
                .withMinChunkSizeChars(30)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(300)
                .build();
        List<Document> documents = splitter1.split(textReader.get());

        vectorStore.add(documents);
    }
}
