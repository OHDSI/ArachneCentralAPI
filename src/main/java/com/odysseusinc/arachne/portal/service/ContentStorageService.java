package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.service.jcr.ArachneFileSourced;
import com.odysseusinc.arachne.portal.service.jcr.ArachneFileMeta;
import com.odysseusinc.arachne.portal.service.jcr.QuerySpec;
import java.io.File;
import java.util.List;

public interface ContentStorageService {

    String getJcrLocationForEntity(Object domainObject, List<String> additionalPathParts);

    ArachneFileSourced getFileByFn(String absoulteFilename);

    ArachneFileSourced getFileByIdentifier(String identifier);

    ArachneFileMeta saveFile(String path, String name, File file, Long createdById);

    void delete(String identifier);

    List<ArachneFileMeta> searchFiles(QuerySpec querySpec);
}
