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

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.Comment;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;

public interface CommentService {
    CommentTopic getTopic(Long id) throws NotExistException;

    Set<CommentTopic> list(Set<CommentTopic> topics, Integer size, Sort sort);

    Comment addComment(Long topicId, Long parentId, Comment comment);

    void deleteComment(Long topicId, Long id) throws NotExistException;

    void deleteComments(List<Comment> comments);

    void deleteTopic(CommentTopic topic);
}
