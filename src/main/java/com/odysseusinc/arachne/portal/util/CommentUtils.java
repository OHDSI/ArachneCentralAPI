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
 * Created: July 07, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.api.v1.dto.CommentTopicDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.Commentable;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.ConversionService;

public class CommentUtils {

    private CommentUtils() {

    }

    public static List<Commentable> getRecentCommentables(ConversionService conversionService, Set<CommentTopic> recentTopics, SubmissionInsightDTO insightDTO) {

        final Stream<Commentable> resultFileDTOStream = insightDTO.getResultFiles().stream();
        final Stream<Commentable> submissionFileDTOStream = insightDTO.getCodeFiles().stream();
        final Map<Long, Commentable> allCommentables = Stream.concat(submissionFileDTOStream, resultFileDTOStream)
                .map(Commentable::clone)
                .collect(Collectors.toMap(Commentable::getCommentTopicId, Function.identity(), (id1, id2) -> id1));
        return recentTopics.stream()
                .map(topic -> {
                    final Commentable commentable = allCommentables.get(topic.getId());
                    commentable.setTopic(conversionService.convert(topic, CommentTopicDTO.class));
                    return commentable;
                })
                .collect(Collectors.toList());
    }
}
