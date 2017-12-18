package com.odysseusinc.arachne.portal.service.impl;


import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.service.ContentStorageService;
import com.odysseusinc.arachne.portal.service.jcr.ArachneFileSourced;
import com.odysseusinc.arachne.portal.service.jcr.ArachneFileMeta;
import com.odysseusinc.arachne.portal.service.jcr.QuerySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springmodules.jcr.JcrTemplate;

@Service
public class ContentStorageServiceImpl implements ContentStorageService {

    public static String PATH_SEPARATOR = "/";
    public static String ENTITY_FILES_DIR = "entities";

    public static String JCR_CONTENT_TYPE = "jcr:contentType";
    public static String JCR_AUTHOR = "jcr:author";

    private JcrTemplate jcrTemplate;
    private ConversionService conversionService;
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public ContentStorageServiceImpl(JcrTemplate jcrTemplate,
                                     ConversionService conversionService,
                                     EntityManagerFactory entityManagerFactory) {

        this.jcrTemplate = jcrTemplate;
        this.conversionService = conversionService;
        this.entityManagerFactory = entityManagerFactory;
    }

    public String getJcrLocationForEntity(Object domainObject, List<String> additionalPathParts) {

        List<String> pathParts = new ArrayList<>(Arrays.asList(ENTITY_FILES_DIR));

        Table entityTable = domainObject.getClass().getAnnotation(Table.class);
        pathParts.add(entityTable.name());

        String entityId = String.valueOf(entityManagerFactory.getPersistenceUnitUtil().getIdentifier(domainObject));
        pathParts.add(entityId);

        pathParts.addAll(additionalPathParts);

        return PATH_SEPARATOR + pathParts.stream()
                .filter(part -> StringUtils.isNotBlank(part) && !part.equals(PATH_SEPARATOR))
                .collect(Collectors.joining(PATH_SEPARATOR));
    }

    @Override
    public ArachneFileSourced getFileByFn(String absoulteFilename) {

        return (ArachneFileSourced) jcrTemplate.execute(session -> {

            Node fileNode = session.getNode(absoulteFilename);
            return getFile(fileNode);
        });
    }

    @Override
    public ArachneFileSourced getFileByIdentifier(String identifier) {

        return (ArachneFileSourced) jcrTemplate.execute(session -> {

            Node fileNode = session.getNodeByIdentifier(identifier);
            return getFile(fileNode);
        });
    }

    @Override
    public ArachneFileMeta saveFile(String path, String name, File file, Long createdById) {

        String fixedPath = fixPath(path);
        return (ArachneFileMeta) jcrTemplate.execute(session -> {

            Node targetDir = getOrAddNestedFolder(session, fixedPath);
            Node node = saveFile(targetDir, name, file, createdById);
            session.save();
            return conversionService.convert(node, ArachneFileMeta.class);
        });
    }

    /*@Override
    public void saveFolder(String path, File directory) throws RepositoryException, IOException {

        String fixedPath = fixPath(path);
        return (String) jcrTemplate.execute(session -> {

            Node targetDir = getOrAddNestedFolder(session, fixedPath);
            return saveFolder(targetDir, directory);
        });
    }*/

    @Override
    public List<ArachneFileMeta> searchFiles(QuerySpec querySpec) {

        List<ArachneFileMeta> resultFileList = (List<ArachneFileMeta>) jcrTemplate.execute(session -> {

            List<ArachneFileMeta> result = new ArrayList<>();

            QueryManager queryManager = session.getWorkspace().getQueryManager();
            String expression = buildQuery(querySpec);

            Query query = queryManager.createQuery(expression, javax.jcr.query.Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator nit = queryResult.getNodes();
            Node childNode;
            while (nit.hasNext()) {
                childNode = nit.nextNode();
                ArachneFileMeta file = conversionService.convert(childNode, ArachneFileMeta.class);
                result.add(file);
            }

            return result;
        });

        return resultFileList;
    }

    @Override
    public void delete(String identifier) {

        jcrTemplate.execute(session -> {

            Node fileNode = session.getNodeByIdentifier(identifier);
            fileNode.remove();
            session.save();
            return null;
        });
    }

    private String fixPath(String path) {

        return path.replace('\\', '/');
    }

    // NOTE:
    // Yes, I haven't found query builder for JCR SQL
    private String buildQuery(QuerySpec querySpec) {

        String query = "SELECT * FROM [nt:base]";
        List<String> conditions = new ArrayList<>();

        if (querySpec.getPath() != null) {
            conditions.add("ischildnode('" + querySpec.getPath() + "')");
        }

        if (querySpec.getName() != null) {
            conditions.add("NAME = '" + querySpec.getName() + "'");
        }

        if (conditions.size() > 0) {
            query += "\n WHERE " + String.join("\n AND ", conditions);
        }

        // Sort by: folders first, then by name
        query += "\n order by [jcr:primaryType] desc, name()";

        return query;
    }

    private Node getOrAddNestedFolder(Session session, String path) throws RepositoryException {

        Node node = session.getRootNode();

        if (!path.equals(PATH_SEPARATOR)) {
            List<String> pathParts = Arrays.stream(path.split(PATH_SEPARATOR)).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            for (String folder : pathParts) {
                node = JcrUtils.getOrAddFolder(node, folder);
            }
        }

        return node;
    }

    private ArachneFileSourced getFile(Node fileNode) throws RepositoryException {

        Node resNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
        InputStream stream = resNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();

        ArachneFileMeta arachneFileMeta = conversionService.convert(fileNode, ArachneFileMeta.class);
        ArachneFileSourced file = new ArachneFileSourced(arachneFileMeta);
        file.setInputStream(stream);

        return file;
    }

    private Node saveFile(Node parentNode, String name, File file, Long createdById) throws RepositoryException, IOException {

        String mimeType = CommonFileUtils.getMimeType(file.getName(), file.getAbsolutePath());
        String contentType = CommonFileUtils.getContentType(file.getName(), file.getAbsolutePath());

        Node fileNode = JcrUtils.getOrAddNode(parentNode, name, JcrConstants.NT_FILE);
        Node resNode = JcrUtils.getOrAddNode(fileNode, JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);

        resNode.addMixin("mix:arachneFile");

        resNode.setProperty(JCR_CONTENT_TYPE, contentType);
        if (createdById != null) {
            resNode.setProperty(JCR_AUTHOR, String.valueOf(createdById));
        }
        resNode.setProperty(JcrConstants.JCR_MIMETYPE, mimeType);
        resNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());

        try (InputStream fileStream = new FileInputStream(file)) {
            Binary binary = resNode.getSession().getValueFactory().createBinary(fileStream);
            resNode.setProperty(JcrConstants.JCR_DATA, binary);
        }

        return fileNode;
    }

    /*private void saveFolder(Node parentNode, File directory) throws RepositoryException, IOException {

        for (File dirEntry : directory.listFiles()) {
            if (dirEntry.isDirectory()) {
                Node childNode = JcrUtils.getOrAddFolder(parentNode, dirEntry.getName());
                saveFolder(childNode, dirEntry);
            } else {
                saveFile(parentNode, dirEntry);
            }
        }
    }*/

    public static String getStringProperty(Node node, String path) throws RepositoryException {

        return getStringProperty(node, path, null);
    }

    private static String getStringProperty(Node node, String path, String defaultVal) throws RepositoryException {

        String value = defaultVal;
        if (node.hasProperty(path)) {
            value = node.getProperty(path).getString();
        }
        return value;
    }
}
