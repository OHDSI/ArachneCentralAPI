package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MultipartFileToUploadFileDTOConverter extends BaseConversionServiceAwareConverter<MultipartFile, UploadFileDTO> {
    private static final String LINK_TYPE = "links";
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartFileToUploadFileDTOConverter.class);

    @Override
    public UploadFileDTO convert(MultipartFile source) {

        UploadFileDTO uploadFileDTO = new UploadFileDTO();
        uploadFileDTO.setFile(source);
        uploadFileDTO.setLabel(source.getOriginalFilename());
        if (LINK_TYPE.equalsIgnoreCase(source.getName())) {
            try {
                uploadFileDTO.setLink(new String(source.getBytes()));
            } catch (IOException e) {
                LOGGER.error("Failed to read link", e);
            }
        }
        return uploadFileDTO;
    }
}
