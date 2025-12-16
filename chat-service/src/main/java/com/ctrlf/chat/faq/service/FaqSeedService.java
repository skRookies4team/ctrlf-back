package com.ctrlf.chat.faq.service;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface FaqSeedService {

    void uploadSeedCsv(MultipartFile file, UUID operatorId);
}
