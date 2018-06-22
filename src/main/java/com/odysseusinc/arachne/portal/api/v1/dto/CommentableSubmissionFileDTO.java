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
 * Created: May 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

public class CommentableSubmissionFileDTO extends SubmissionFileDTO implements Commentable {

    protected Long commentTopicId;
    protected CommentTopicDTO topic;
    protected Long commentCount;

    @Override
    public Long getCommentTopicId() {

        return commentTopicId;
    }

    @Override
    public void setCommentTopicId(Long commentTopicId) {

        this.commentTopicId = commentTopicId;
    }

    @Override
    public void setCommentCount(Long commentCount) {

        this.commentCount = commentCount;
    }

    @Override
    public Long getCommentCount() {

        return commentCount;
    }

    @Override
    public CommentTopicDTO getTopic() {

        return topic;
    }

    @Override
    public void setTopic(CommentTopicDTO topic) {

        this.topic = topic;
    }

    @Override
    public Commentable clone() {

        final CommentableSubmissionFileDTO clone = new CommentableSubmissionFileDTO();
        clone.setCommentTopicId(commentTopicId);
        clone.setTopic(topic);
        clone.setCommentCount(commentCount);
        clone.setUuid(uuid);
        clone.setName(name);
        clone.setLabel(label);
        clone.setCreated(created);
        clone.setDocType(docType);
        clone.setAuthor(author);
        return clone;
    }
}
