package com.odysseusinc.arachne.portal.service.jcr;

import static com.odysseusinc.arachne.portal.service.impl.ContentStorageServiceImpl.JCR_AUTHOR;
import static com.odysseusinc.arachne.portal.service.impl.ContentStorageServiceImpl.JCR_CONTENT_TYPE;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.service.impl.ContentStorageServiceImpl;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jackrabbit.JcrConstants;
import org.springframework.stereotype.Component;

@Component
public class JcrNodeToArachneFileMeta extends BaseConversionServiceAwareConverter<Node, ArachneFileMeta> {

    @Override
    public ArachneFileMeta convert(Node node) {

        ArachneFileMetaImpl result = new ArachneFileMetaImpl();

        try {
            result.setUuid(node.getIdentifier());
            result.setName(node.getName());
            result.setPath(node.getPath());
            result.setCreated(node.getProperty(JcrConstants.JCR_CREATED).getDate().getTime());

            if (node.hasNode(JcrConstants.JCR_CONTENT)) {
                Node resNode = node.getNode(JcrConstants.JCR_CONTENT);
                result.setUpdated(resNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().getTime());
                result.setContentType(ContentStorageServiceImpl.getStringProperty(resNode, JCR_CONTENT_TYPE));
                result.setCreatedBy(NumberUtils.createLong(ContentStorageServiceImpl.getStringProperty(resNode, JCR_AUTHOR)));
            } else {
                result.setContentType(CommonFileUtils.TYPE_FOLDER);
            }
        } catch (RepositoryException ex) {
            return null;
        }

        return result;
    }
}
