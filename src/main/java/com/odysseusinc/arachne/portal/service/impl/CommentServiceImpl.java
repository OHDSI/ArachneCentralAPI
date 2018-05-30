/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: April 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.Comment;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.repository.CommentRepository;
import com.odysseusinc.arachne.portal.repository.CommentTopicRepository;
import com.odysseusinc.arachne.portal.service.CommentService;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private static final String TOPIC_NOT_EXIST_EXCEPTION = "Comment Topic with id='%s' is not exist";
    private static final String COMMENT_NOT_EXIST_EXCEPTION = "Comment with topicId='%s' and id='%s' is not exist";

    private final CommentTopicRepository commentTopicRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentTopicRepository commentTopicRepository,
                              CommentRepository commentRepository) {

        this.commentTopicRepository = commentTopicRepository;
        this.commentRepository = commentRepository;
    }

//    @Cacheable("comments") broken code here
    @Override
    public CommentTopic getTopic(Long id) throws NotExistException {

        final CommentTopic commentTopic = commentTopicRepository.getOne(id);
        if (commentTopic == null) {
            final String message = String.format(TOPIC_NOT_EXIST_EXCEPTION, id);
            throw new NotExistException(message, CommentTopic.class);
        }
        final List<Comment> comments = commentRepository.getAllByTopicIdAndParentIsNull(id);
        comments.forEach(c -> Hibernate.initialize(c.getAuthor().getRoles()));
        commentTopic.setComments(comments);
        return commentTopic;
    }

    @Override
    public Set<CommentTopic> list(Set<CommentTopic> topics, Integer size, Sort sort) {

        Pageable pageable = new PageRequest(0, size, sort);
        final Page<Comment> page = commentRepository.getAllByTopicIn(topics, pageable);
        final List<Comment> comments = page.getContent();
        comments.forEach(comment -> connectToTopic(topics, comment));
        return comments.stream()
                .map(Comment::getTopic)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void connectToTopic(Set<CommentTopic> topics, Comment comment) {

        comment.setParent(null);
        final Long id = comment.getTopic().getId();
        topics.stream()
                .filter(commentTopic -> id.equals(commentTopic.getId()))
                .findFirst()
                .ifPresent(commentTopic -> commentTopic.getComments().add(comment));
    }

    @CacheEvict(cacheNames = "comments", allEntries = true)
    @Override
    @PreAuthorize("hasPermission(#topicId, 'CommentTopic', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_INSIGHT)")
    public Comment addComment(Long topicId, Long parentId, Comment comment) {

        comment.setId(null);
        comment.setDate(new Date());
        final CommentTopic topic = commentTopicRepository.getOne(topicId);
        comment.setTopic(topic);
        if (parentId != null) {
            final Comment parent = commentRepository.findOne(parentId);
            comment.setParent(parent);
        }
        return commentRepository.save(comment);
    }

    @CacheEvict(cacheNames = "comments", allEntries = true)
    @Override
    public void deleteComment(Long topicId, Long commentId) throws NotExistException {

        final Long count = commentRepository.deleteByTopicIdAndId(topicId, commentId);
        if (count == 0) {
            final String message = String.format(COMMENT_NOT_EXIST_EXCEPTION, topicId, commentId);
            throw new NotExistException(message, CommentTopic.class);
        }
    }

    @Override
    public void deleteComments(List<Comment> comments) {

        commentRepository.delete(comments);
    }

    @Override
    public void deleteTopic(CommentTopic topic) {

        commentTopicRepository.delete(topic);
    }
}
